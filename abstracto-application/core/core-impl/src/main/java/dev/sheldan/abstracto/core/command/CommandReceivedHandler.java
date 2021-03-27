package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.exception.CommandParameterValidationException;
import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.CommandParameterHandler;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.*;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Service
@Slf4j
public class CommandReceivedHandler extends ListenerAdapter {

    @Autowired
    private CommandManager commandManager;

    @Autowired
    private List<PostCommandExecution> executions;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    @Lazy
    private CommandReceivedHandler self;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private CommandService commandService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired
    private EmoteManagementService emoteManagementService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private List<CommandParameterHandler> parameterHandlers;

    @Autowired
    private MetricService metricService;

    public static final String COMMAND_PROCESSED = "command.processed";
    public static final String STATUS_TAG = "status";
    public static final CounterMetric COMMANDS_PROCESSED_COUNTER = CounterMetric.builder().name(COMMAND_PROCESSED).tagList(Arrays.asList(MetricTag.getTag(STATUS_TAG, "processed"))).build();
    public static final CounterMetric COMMANDS_WRONG_PARAMETER_COUNTER = CounterMetric.builder().name(COMMAND_PROCESSED).tagList(Arrays.asList(MetricTag.getTag(STATUS_TAG, "parameter.wrong"))).build();

    @Override
    @Transactional
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild()) {
            return;
        }
        if(!commandManager.isCommand(event.getMessage())) {
            return;
        }
        metricService.incrementCounter(COMMANDS_PROCESSED_COUNTER);
        final Command foundCommand;
        try {
            String contentStripped = event.getMessage().getContentRaw();
            List<String> parameters = Arrays.asList(contentStripped.split(" "));
            UnParsedCommandParameter unParsedParameter = new UnParsedCommandParameter(contentStripped, event.getMessage());
            String commandName = commandManager.getCommandName(parameters.get(0), event.getGuild().getIdLong());
            foundCommand = commandManager.findCommandByParameters(commandName, unParsedParameter, event.getGuild().getIdLong());
            tryToExecuteFoundCommand(event, foundCommand, unParsedParameter);

        } catch (Exception e) {
            reportException(event, null, e, "Exception when executing command.");
        }
    }

    private void tryToExecuteFoundCommand(MessageReceivedEvent event, Command foundCommand, UnParsedCommandParameter unParsedParameter) {
        CompletableFuture<Parameters> parsingFuture = getParsedParameters(unParsedParameter, foundCommand, event.getMessage());
        parsingFuture.thenAccept(parsedParameters ->
            self.executeCommand(event, foundCommand, parsedParameters)
        ).exceptionally(throwable -> {
            self.reportException(event, foundCommand, throwable, "Exception when parsing command.");
            return null;
        });
    }

    @Transactional
    public void executeCommand(MessageReceivedEvent event, Command foundCommand, Parameters parsedParameters) {
        UserInitiatedServerContext userInitiatedContext = buildTemplateParameter(event);
        CommandContext.CommandContextBuilder commandContextBuilder = CommandContext.builder()
                .author(event.getMember())
                .guild(event.getGuild())
                .undoActions(new ArrayList<>())
                .channel(event.getTextChannel())
                .message(event.getMessage())
                .jda(event.getJDA())
                .userInitiatedContext(userInitiatedContext);
        validateCommandParameters(parsedParameters, foundCommand);
        CommandContext commandContext = commandContextBuilder.parameters(parsedParameters).build();
        ConditionResult conditionResult = commandService.isCommandExecutable(foundCommand, commandContext);
        CommandResult commandResult = null;
        if(conditionResult.isResult()) {
            if(foundCommand.getConfiguration().isAsync()) {
                log.info("Executing async command {} for server {} in channel {} based on message {} by user {}.",
                        foundCommand.getConfiguration().getName(), commandContext.getGuild().getId(), commandContext.getChannel().getId(), commandContext.getMessage().getId(), commandContext.getAuthor().getId());

                self.executeAsyncCommand(foundCommand, commandContext).exceptionally(throwable -> {
                    log.error("Asynchronous command {} failed.", foundCommand.getConfiguration().getName(), throwable);
                    UserInitiatedServerContext rebuildUserContext = buildTemplateParameter(event);
                    CommandContext rebuildContext = CommandContext.builder()
                            .author(event.getMember())
                            .guild(event.getGuild())
                            .channel(event.getTextChannel())
                            .message(event.getMessage())
                            .jda(event.getJDA())
                            .undoActions(commandContext.getUndoActions()) // TODO really do this? it would need to guarantee that its available and usable
                            .userInitiatedContext(rebuildUserContext)
                            .parameters(parsedParameters).build();
                    CommandResult failedResult = CommandResult.fromError(throwable.getMessage(), throwable);
                    self.executePostCommandListener(foundCommand, rebuildContext, failedResult);
                    return null;
                });
            } else {
                commandResult = self.executeCommand(foundCommand, commandContext);
            }
        } else {
            commandResult = CommandResult.fromCondition(conditionResult);
        }
        if(commandResult != null) {
            self.executePostCommandListener(foundCommand, commandContext, commandResult);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CompletableFuture<Void> executeAsyncCommand(Command foundCommand, CommandContext commandContext) {
        return foundCommand.executeAsync(commandContext).thenAccept(result ->
                executePostCommandListener(foundCommand, commandContext, result)
        );
    }

    @Transactional
    public void reportException(MessageReceivedEvent event, Command foundCommand, Throwable throwable, String s) {
        UserInitiatedServerContext userInitiatedContext = buildTemplateParameter(event);
        CommandContext.CommandContextBuilder commandContextBuilder = CommandContext.builder()
                .author(event.getMember())
                .guild(event.getGuild())
                .undoActions(new ArrayList<>())
                .channel(event.getTextChannel())
                .message(event.getMessage())
                .jda(event.getJDA())
                .userInitiatedContext(userInitiatedContext);
        log.error(s, throwable);
        CommandResult commandResult = CommandResult.fromError(throwable.getMessage(), throwable);
        CommandContext commandContext = commandContextBuilder.build();
        self.executePostCommandListener(foundCommand, commandContext, commandResult);
    }

    private void validateCommandParameters(Parameters parameters, Command foundCommand) {
        List<Parameter> parameterList = foundCommand.getConfiguration().getParameters();
        // we iterate only over the actually found parameters, that way we dont have to consider the optional parameters
        // the parameters are going from left to right anyway
        for (int i = 0; i < parameters.getParameters().size(); i++) {
            Parameter parameter = parameterList.get(i);
            for (ParameterValidator parameterValidator : parameter.getValidators()) {
                boolean validate = parameterValidator.validate(parameters.getParameters().get(i));
                if(!validate) {
                    log.trace("Parameter {} in command {} failed to validate.", parameter.getName(), foundCommand.getConfiguration().getName());
                    throw new CommandParameterValidationException(parameterValidator.getParameters(), parameterValidator.getExceptionTemplateName(), parameter);
                }
            }
        }
    }

    @Transactional
    public void executePostCommandListener(Command foundCommand, CommandContext commandContext, CommandResult result) {
        log.trace("Executing post command listeners for command from message {}.", commandContext.getMessage().getIdLong());
        for (PostCommandExecution postCommandExecution : executions) {
            postCommandExecution.execute(commandContext, result, foundCommand);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CommandResult executeCommand(Command foundCommand, CommandContext commandContext) {
        log.info("Executing sync command {} for server {} in channel {} based on message {} by user {}.",
                foundCommand.getConfiguration().getName(), commandContext.getGuild().getId(), commandContext.getChannel().getId(), commandContext.getMessage().getId(), commandContext.getAuthor().getId());
        return foundCommand.execute(commandContext);
    }

    private UserInitiatedServerContext buildTemplateParameter(MessageReceivedEvent event) {
        return UserInitiatedServerContext
                .builder()
                .member(event.getMember())
                .messageChannel(event.getTextChannel())
                .guild(event.getGuild())
                .build();
    }

    public CompletableFuture<Parameters> getParsedParameters(UnParsedCommandParameter unParsedCommandParameter, Command command, Message message){
        List<Object> parsedParameters = new ArrayList<>();
        if(command.getConfiguration().getParameters() == null || command.getConfiguration().getParameters().isEmpty()) {
            return CompletableFuture.completedFuture(Parameters.builder().parameters(parsedParameters).build());
        }
        log.trace("Parsing parameters for command {} based on message {}.", command.getConfiguration().getName(), message.getId());
        Iterator<TextChannel> channelIterator = message.getMentionedChannels().iterator();
        Iterator<Emote> emoteIterator = message.getEmotesBag().iterator();
        Iterator<Member> memberIterator = message.getMentionedMembers().iterator();
        Iterator<Role> roleIterator = message.getMentionedRolesBag().iterator();
        Parameter param = command.getConfiguration().getParameters().get(0);
        CommandParameterIterators iterators = new CommandParameterIterators(channelIterator, emoteIterator, memberIterator, roleIterator);
        boolean reminderActive = false;
        List<CompletableFuture> futures = new ArrayList<>();
        for (int i = 0; i < unParsedCommandParameter.getParameters().size(); i++) {
            if(i < command.getConfiguration().getParameters().size() && !param.isRemainder()) {
                param = command.getConfiguration().getParameters().get(i);
            } else {
                reminderActive = true;
            }
            UnparsedCommandParameterPiece value = unParsedCommandParameter.getParameters().get(i);
            boolean handlerMatched = false;
            for (CommandParameterHandler handler : parameterHandlers) {
                try {
                    if (handler.handles(param.getType())) {
                        handlerMatched = true;
                        if (handler.async()) {
                            CompletableFuture future = handler.handleAsync(value, iterators, param.getType(), message);
                            futures.add(future);
                            parsedParameters.add(future);
                        } else {
                            parsedParameters.add(handler.handle(value, iterators, param.getType(), message));
                        }
                        break;
                    }
                } catch (AbstractoRunTimeException abstractoRunTimeException) {
                    throw abstractoRunTimeException;
                } catch (Exception e) {
                    log.warn("Failed to parse parameter with exception.", e);
                    metricService.incrementCounter(COMMANDS_WRONG_PARAMETER_COUNTER);
                    throw new IncorrectParameterException(command, param.getName());
                }
            }
            if(!handlerMatched) {
                Object valueAsString = value.getValue().toString();
                if(!reminderActive) {
                    parsedParameters.add(valueAsString);
                } else {
                    if(!param.isListParam()) {
                        if(parsedParameters.isEmpty()) {
                            parsedParameters.add(valueAsString);
                        } else {
                            int lastIndex = parsedParameters.size() - 1;
                            parsedParameters.set(lastIndex, parsedParameters.get(lastIndex) + " " + valueAsString);
                        }
                    } else {
                        if(parsedParameters.isEmpty()) {
                            ArrayList<Object> list = new ArrayList<>();
                            list.add(valueAsString);
                            parsedParameters.add(list);
                        } else {
                            int lastIndex = parsedParameters.size() - 1;
                            ((List)parsedParameters.get(lastIndex)).add(valueAsString);
                        }
                    }
                }
            }
        }

        if(!futures.isEmpty()) {
            CompletableFuture<Parameters> multipleFuturesFuture = new CompletableFuture<>();
            CompletableFuture<Void> combinedFuture = FutureUtils.toSingleFuture(futures);
            combinedFuture.thenAccept(aVoid -> {
                List<Object> usableParameters = parsedParameters.stream().map(o -> {
                    if(o instanceof CompletableFuture) {
                        return ((CompletableFuture) o).join();
                    } else {
                        return o;
                    }
                }).collect(Collectors.toList());
                multipleFuturesFuture.complete(Parameters.builder().parameters(usableParameters).build());
            });

            combinedFuture.exceptionally(throwable -> {
                multipleFuturesFuture.completeExceptionally(throwable);
                return null;
            });
            return multipleFuturesFuture;
        } else {
            Parameters parameters = Parameters.builder().parameters(parsedParameters).build();
            return CompletableFuture.completedFuture(parameters);
        }
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(COMMANDS_PROCESSED_COUNTER, "Commands processed");
        metricService.registerCounter(COMMANDS_WRONG_PARAMETER_COUNTER, "Commands with incorrect parameter");
        this.parameterHandlers = parameterHandlers.stream().sorted(comparing(CommandParameterHandler::getPriority)).collect(Collectors.toList());
    }
}
