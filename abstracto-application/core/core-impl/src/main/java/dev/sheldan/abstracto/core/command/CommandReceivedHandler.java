package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.config.*;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureConfig;
import dev.sheldan.abstracto.core.command.exception.CommandParameterValidationException;
import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.command.handler.CommandParameterHandler;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.interaction.button.CommandConfirmationModel;
import dev.sheldan.abstracto.core.interaction.button.CommandConfirmationPayload;
import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadService;
import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.*;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
    @Lazy
    private CommandReceivedHandler self;

    @Autowired
    private CommandService commandService;

    @Autowired
    private List<CommandParameterHandler> parameterHandlers;

    @Autowired(required = false)
    private List<CommandAlternative> commandAlternatives;

    @Autowired
    private MetricService metricService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private ComponentPayloadService componentPayloadService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private MessageService messageService;

    public static final String COMMAND_CONFIRMATION_ORIGIN = "commandConfirmation";
    public static final String COMMAND_CONFIRMATION_MESSAGE_TEMPLATE_KEY = "command_confirmation_message";
    public static final String COMMAND_PROCESSED = "command.processed";
    public static final String STATUS_TAG = "status";
    public static final String TYPE_TAG = "type";
    public static final CounterMetric COMMANDS_PROCESSED_COUNTER = CounterMetric
            .builder()
            .name(COMMAND_PROCESSED)
            .tagList(Arrays.asList(MetricTag.getTag(STATUS_TAG, "processed"), MetricTag.getTag(TYPE_TAG, "message")))
            .build();
    public static final CounterMetric COMMANDS_WRONG_PARAMETER_COUNTER = CounterMetric
            .builder()
            .name(COMMAND_PROCESSED)
            .tagList(Arrays.asList(MetricTag.getTag(STATUS_TAG, "parameter.wrong"), MetricTag.getTag(TYPE_TAG, "message")))
            .build();

    @Override
    @Transactional
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }
        Message message = event.getMessage();
        if (!commandManager.isCommand(message)) {
            return;
        }
        metricService.incrementCounter(COMMANDS_PROCESSED_COUNTER);
        try {
            UnParsedCommandResult result = getUnparsedCommandResult(message);
            if(result.getCommand() != null) {
                CompletableFuture<CommandParseResult> parsingFuture = getParametersFromMessage(message, result);
                parsingFuture.thenAccept(parsedParameters -> {
                    try {
                        self.executeCommand(event, parsedParameters.getCommand(), parsedParameters.getParameters());
                    } catch (Exception e) {
                        reportException(event, null, e, String.format("Exception when executing command from message %d in message %d in guild %d."
                                , message.getIdLong(), event.getChannel().getIdLong(), event.getGuild().getIdLong()));
                    }
                });
                parsingFuture.exceptionally(throwable -> {
                    self.reportException(event, result.getCommand(), throwable, "Exception when parsing command.");
                    return null;
                });
            } else {
                if(commandAlternatives != null) {
                    Optional<CommandAlternative> foundAlternativeOptional = commandAlternatives
                            .stream()
                            .filter(commandAlternative -> commandAlternative.shouldExecute(result.getParameter(), event.getGuild()))
                            .findFirst();
                    if(foundAlternativeOptional.isPresent()) {
                        CommandAlternative foundAlternative = foundAlternativeOptional.get();
                        log.info("Found alternative {} to execute for command.", foundAlternative.getClass());
                        foundAlternative.execute(result.getParameter(), message);
                    }
                }
            }
        } catch (Exception e) {
            reportException(event, null, e, String.format("Exception when executing command from message %d in message %d in guild %d."
                    , message.getIdLong(), event.getChannel().getIdLong(), event.getGuild().getIdLong()));
        }
    }

    public UnParsedCommandResult getUnparsedCommandResult(Message message) {
        String contentStripped = message.getContentRaw();
        List<String> parameters = Arrays.asList(contentStripped.split(" "));
        UnParsedCommandParameter unParsedParameter = new UnParsedCommandParameter(contentStripped, message);
        String commandName = commandManager.getCommandName(parameters.get(0), message.getGuild().getIdLong());
        Command foundCommand = commandManager.findCommandByParameters(commandName, unParsedParameter, message.getGuild().getIdLong()).orElse(null);
        return UnParsedCommandResult
                .builder()
                .command(foundCommand)
                .parameter(unParsedParameter)
                .build();
    }

    public CompletableFuture<CommandParseResult> getParametersFromMessage(Message message) {
        UnParsedCommandResult result = getUnparsedCommandResult(message);
        return getParsedParameters(result.getParameter(), result.getCommand(), message).thenApply(foundParameters -> CommandParseResult
                .builder()
                .command(result.getCommand())
                .parameters(foundParameters)
                .build());
    }

    public CompletableFuture<CommandParseResult> getParametersFromMessage(Message message, UnParsedCommandResult result) {
        return getParsedParameters(result.getParameter(), result.getCommand(), message).thenApply(foundParameters -> CommandParseResult
                .builder()
                .command(result.getCommand())
                .parameters(foundParameters)
                .build());
    }

    @Transactional
    public CompletableFuture<Void> cleanupConfirmationMessage(Long server, Long channelId, Long messageId, String confirmationPayloadId, String abortPayloadId) {
        componentPayloadManagementService.deletePayloads(Arrays.asList(confirmationPayloadId, abortPayloadId));
        return messageService.deleteMessageInChannelInServer(server, channelId, messageId);
    }

    @Transactional
    public void persistConfirmationCallbacks(CommandConfirmationModel model, Message createdMessage) {
        AServer server = serverManagementService.loadServer(model.getDriedCommandContext().getServerId());
        CommandConfirmationPayload confirmPayload = CommandConfirmationPayload
                .builder()
                .commandContext(model.getDriedCommandContext())
                .otherButtonComponentId(model.getAbortButtonId())
                .action(CommandConfirmationPayload.CommandConfirmationAction.CONFIRM)
                .build();

        componentPayloadService.createButtonPayload(model.getConfirmButtonId(), confirmPayload, COMMAND_CONFIRMATION_ORIGIN, server);
        CommandConfirmationPayload abortPayload = CommandConfirmationPayload
                .builder()
                .commandContext(model.getDriedCommandContext())
                .otherButtonComponentId(model.getConfirmButtonId())
                .action(CommandConfirmationPayload.CommandConfirmationAction.ABORT)
                .build();
        componentPayloadService.createButtonPayload(model.getAbortButtonId(), abortPayload, COMMAND_CONFIRMATION_ORIGIN, server);
        scheduleConfirmationDeletion(createdMessage, model.getConfirmButtonId(), model.getAbortButtonId());
    }

    private void scheduleConfirmationDeletion(Message createdMessage, String confirmationPayloadId, String abortPayloadId) {
        HashMap<Object, Object> parameters = new HashMap<>();
        Long serverId = createdMessage.getGuild().getIdLong();
        parameters.put("serverId", serverId.toString());
        parameters.put("channelId", createdMessage.getChannel().getId());
        parameters.put("messageId", createdMessage.getId());
        parameters.put("confirmationPayloadId", confirmationPayloadId);
        parameters.put("abortPayloadId", abortPayloadId);
        JobParameters jobParameters = JobParameters
                .builder()
                .parameters(parameters)
                .build();
        Long confirmationTimeout = configService.getLongValueOrConfigDefault(CoreFeatureConfig.CONFIRMATION_TIMEOUT, serverId);
        Instant targetDate = Instant.now().plus(confirmationTimeout, ChronoUnit.SECONDS);
        long channelId = createdMessage.getChannel().getIdLong();
        log.info("Scheduling job to delete confirmation message {} in channel {} in server {} at {}.", createdMessage.getIdLong(), channelId, serverId, targetDate);
        schedulerService.executeJobWithParametersOnce("confirmationCleanupJob", "core", jobParameters, Date.from(targetDate));
    }

    @Transactional(rollbackFor = AbstractoRunTimeException.class)
    public void executeCommand(MessageReceivedEvent event, Command foundCommand, Parameters parsedParameters) {
        UserInitiatedServerContext userInitiatedContext = buildUserInitiatedServerContext(event);
        CommandContext.CommandContextBuilder commandContextBuilder = CommandContext.builder()
                .author(event.getMember())
                .guild(event.getGuild())
                .undoActions(new ArrayList<>())
                .channel(event.getGuildChannel())
                .message(event.getMessage())
                .jda(event.getJDA())
                .userInitiatedContext(userInitiatedContext);
        validateCommandParameters(parsedParameters, foundCommand);
        CommandContext commandContext = commandContextBuilder.parameters(parsedParameters).build();
        CompletableFuture<ConditionResult> conditionResultFuture = commandService.isCommandExecutable(foundCommand, commandContext);
        conditionResultFuture.thenAccept(conditionResult -> {
            CommandResult commandResult = null;
            if (conditionResult.isResult()) {
                CommandConfiguration commandConfiguration = foundCommand.getConfiguration();
                if (commandConfiguration.isRequiresConfirmation()) {
                    DriedCommandContext driedCommandContext = DriedCommandContext.buildFromCommandContext(commandContext);
                    driedCommandContext.setCommandName(commandConfiguration.getName());
                    String confirmId = componentService.generateComponentId();
                    String abortId = componentService.generateComponentId();
                    CommandConfirmationModel model = CommandConfirmationModel
                            .builder()
                            .abortButtonId(abortId)
                            .confirmButtonId(confirmId)
                            .driedCommandContext(driedCommandContext)
                            .commandName(commandConfiguration.getName())
                            .build();
                    MessageToSend message = templateService.renderEmbedTemplate(COMMAND_CONFIRMATION_MESSAGE_TEMPLATE_KEY, model, event.getGuild().getIdLong());
                    List<CompletableFuture<Message>> confirmationMessageFutures = channelService.sendMessageToSendToChannel(message, event.getChannel());
                    FutureUtils.toSingleFutureGeneric(confirmationMessageFutures)
                            .thenAccept(unused -> self.persistConfirmationCallbacks(model, confirmationMessageFutures.get(0).join()))
                            .exceptionally(throwable -> self.handleFailedCommand(foundCommand, commandContext, throwable));
                } else if (commandConfiguration.isAsync()) {
                    log.info("Executing async command {} for server {} in channel {} based on message {} by user {}.",
                            commandConfiguration.getName(), commandContext.getGuild().getId(), commandContext.getChannel().getId(), commandContext.getMessage().getId(), commandContext.getAuthor().getId());

                    self.executeAsyncCommand(foundCommand, commandContext)
                            .exceptionally(throwable -> handleFailedCommand(foundCommand, commandContext, throwable));
                } else {
                    commandResult = self.executeCommand(foundCommand, commandContext);
                }
            } else {
                commandResult = CommandResult.fromCondition(conditionResult);
            }
            if (commandResult != null) {
                self.executePostCommandListener(foundCommand, commandContext, commandResult);
            }
        }).exceptionally(throwable -> handleFailedCommand(foundCommand, commandContext, throwable));

    }

    private Void handleFailedCommand(Command foundCommand, CommandContext commandContext, Throwable throwable) {
        log.error("Asynchronous command {} failed.", foundCommand.getConfiguration().getName(), throwable);
        CommandResult failedResult = CommandResult.fromError(throwable.getMessage(), throwable);
        self.executePostCommandListener(foundCommand, commandContext, failedResult);
        return null;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = AbstractoRunTimeException.class)
    public CompletableFuture<Void> executeAsyncCommand(Command foundCommand, CommandContext commandContext) {
        return foundCommand.executeAsync(commandContext).thenAccept(result ->
                executePostCommandListener(foundCommand, commandContext, result)
        ).exceptionally(throwable -> {
            handleFailedCommand(foundCommand, commandContext, throwable);
            return null;
        });
    }

    @Transactional
    public void reportException(CommandContext context, Command foundCommand, Throwable throwable, String s) {
        reportException(context.getMessage(), context.getChannel(), context.getAuthor(), foundCommand, throwable, s);
    }

    @Transactional
    public void reportException(Message message, MessageChannel channel, Member member, Command foundCommand, Throwable throwable, String s) {
        UserInitiatedServerContext userInitiatedContext = buildUserInitiatedServerContext(member, channel, member.getGuild());
        CommandContext.CommandContextBuilder commandContextBuilder = CommandContext.builder()
                .author(member)
                .guild(message.getGuild())
                .undoActions(new ArrayList<>())
                .channel(message.getGuildChannel())
                .message(message)
                .jda(message.getJDA())
                .userInitiatedContext(userInitiatedContext);
        log.error(s, throwable);
        CommandResult commandResult = CommandResult.fromError(throwable.getMessage(), throwable);
        CommandContext commandContext = commandContextBuilder.build();
        self.executePostCommandListener(foundCommand, commandContext, commandResult);
    }

    @Transactional
    public void reportException(MessageReceivedEvent event, Command foundCommand, Throwable throwable, String s) {
        reportException(event.getMessage(), event.getChannel(), event.getMember(), foundCommand, throwable, s);
    }

    private void validateCommandParameters(Parameters parameters, Command foundCommand) {
        CommandConfiguration commandConfiguration = foundCommand.getConfiguration();
        List<Parameter> parameterList = commandConfiguration.getParameters();
        // we iterate only over the actually found parameters, that way we dont have to consider the optional parameters
        // the parameters are going from left to right anyway
        for (int i = 0; i < parameters.getParameters().size(); i++) {
            Parameter parameter = parameterList.get(Math.min(i, parameterList.size() - 1));
            for (ParameterValidator parameterValidator : parameter.getValidators()) {
                boolean validate = parameterValidator.validate(parameters.getParameters().get(i));
                if (!validate) {
                    log.debug("Parameter {} in command {} failed to validate.", parameter.getName(), commandConfiguration.getName());
                    throw new CommandParameterValidationException(parameterValidator.getParameters(), parameterValidator.getExceptionTemplateName(), parameter);
                }
            }
        }
        if (commandConfiguration.getNecessaryParameterCount() > parameters.getParameters().size()) {
            String nextParameterName = commandConfiguration.getParameters().get(commandConfiguration.getNecessaryParameterCount() - 1).getName();
            throw new InsufficientParametersException(foundCommand, nextParameterName);
        }
    }

    @Transactional
    public void executePostCommandListener(Command foundCommand, CommandContext commandContext, CommandResult result) {
        log.debug("Executing post command listeners for command from message {}.", commandContext.getMessage().getIdLong());
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

    private UserInitiatedServerContext buildUserInitiatedServerContext(Member member, MessageChannel channel, Guild guild) {
        return UserInitiatedServerContext
                .builder()
                .member(member)
                .messageChannel(channel)
                .guild(guild)
                .build();
    }

    private UserInitiatedServerContext buildUserInitiatedServerContext(MessageReceivedEvent event) {
        return buildUserInitiatedServerContext(event.getMember(), event.getChannel(), event.getGuild());
    }

    public CompletableFuture<Parameters> getParsedParameters(UnParsedCommandParameter unParsedCommandParameter, Command command, Message message) {
        List<ParseResult> parsedParameters = new ArrayList<>();
        List<Parameter> parameters = command
                .getConfiguration()
                .getParameters()
                .stream()
                .filter(parameter -> !parameter.getSlashCommandOnly()).collect(Collectors.toList());
        if (parameters == null || parameters.isEmpty()) {
            return CompletableFuture.completedFuture(Parameters.builder().parameters(new ArrayList<>()).build());
        }
        log.debug("Parsing parameters for command {} based on message {}.", command.getConfiguration().getName(), message.getId());
        Iterator<TextChannel> channelIterator = message
                .getMentions()
                .getChannels()
                .stream()
                .filter(TextChannel.class::isInstance)
                .map(TextChannel.class::cast)
                .iterator();
        Iterator<CustomEmoji> emoteIterator = message
                .getMentions()
                .getCustomEmojisBag()
                .iterator();
        Iterator<Member> memberIterator = message
                .getMentions()
                .getMembers()
                .iterator();
        Iterator<Role> roleIterator = message.getMentions().getRolesBag().iterator();
        Parameter param = parameters.get(0);
        CommandParameterIterators iterators = new CommandParameterIterators(channelIterator, emoteIterator, memberIterator, roleIterator);
        Set<CommandParameterHandler> usedParameterHandler = findNecessaryCommandParameterHandlers(parameters, unParsedCommandParameter);
        List<CompletableFuture> futures = new ArrayList<>();
        // the actual parameters which were handled, might not coincide with the unparsed parameters
        // because we might ignore some parameters (for example referenced messages) in case the command does not use this as a parameter
        int parsedParameter = 0;
        for (int i = 0; i < unParsedCommandParameter.getParameters().size(); i++) {
            if (parsedParameter < parameters.size() && !param.isRemainder()) {
                param = parameters.get(parsedParameter);
            } else if (param.isRemainder()) {
                param = parameters.get(parameters.size() - 1);
            } else {
                break;
            }
            UnparsedCommandParameterPiece value = unParsedCommandParameter.getParameters().get(i);
            // TODO might be able to do this without iterating, if we directly associated the handler required for each parameter
            for (CommandParameterHandler handler : usedParameterHandler) {
                try {
                    if (handler.handles(param.getType(), value)) {
                        if (handler.async()) {
                            CompletableFuture future = handler.handleAsync(value, iterators, param, message, command);
                            futures.add(future);
                            parsedParameters.add(ParseResult.builder().parameter(param).result(future).build());
                        } else {
                            Object result = handler.handle(value, iterators, param, message, command);
                            if (result != null) {
                                parsedParameters.add(ParseResult.builder().parameter(param).result(result).build());
                            }
                        }
                        parsedParameter++;
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
        }

        if (!futures.isEmpty()) {
            CompletableFuture<Parameters> multipleFuturesFuture = new CompletableFuture<>();
            CompletableFuture<Void> combinedFuture = FutureUtils.toSingleFuture(futures);
            combinedFuture.thenAccept(aVoid -> {
                List<Object> allParamResults = parsedParameters.stream().map(o -> {
                    if (o.getResult() instanceof CompletableFuture) {
                        return ((CompletableFuture) o.getResult()).join();
                    } else {
                        return o.getResult();
                    }
                }).collect(Collectors.toList());
                List<ParseResult> parseResults = new ArrayList<>();
                for (int i = 0; i < allParamResults.size(); i++) {
                    if (allParamResults.get(i) != null) {
                        ParseResult parseResult = ParseResult
                                .builder()
                                .result(allParamResults.get(i))
                                // all parameters beyond the most possible ones are attributed to be from the last parameter
                                .parameter(parameters.get(Math.min(i, parameters.size() - 1)))
                                .build();
                        parseResults.add(parseResult);
                    }
                }
                multipleFuturesFuture.complete(Parameters.builder().parameters(extractParametersFromParsed(parseResults)).build());
            });

            combinedFuture.exceptionally(throwable -> {
                multipleFuturesFuture.completeExceptionally(throwable);
                return null;
            });
            return multipleFuturesFuture;
        } else {
            Parameters resultParameters = Parameters.builder().parameters(extractParametersFromParsed(parsedParameters)).build();
            return CompletableFuture.completedFuture(resultParameters);
        }
    }

    private Set<CommandParameterHandler> findNecessaryCommandParameterHandlers(List<Parameter> parameters, UnParsedCommandParameter unParsedCommandParameter) {
        Set<CommandParameterHandler> foundHandlers = new HashSet<>();
        Parameter param = parameters.get(0);
        int parsedParameter = 0;
        for (int i = 0; i < unParsedCommandParameter.getParameters().size(); i++) {
            if (parsedParameter < parameters.size() && !param.isRemainder()) {
                param = parameters.get(parsedParameter);
            } else {
                param = parameters.get(parameters.size() - 1);
            }
            UnparsedCommandParameterPiece value = unParsedCommandParameter.getParameters().get(i);
            for (Parameter parameter : parameters) {
                for (CommandParameterHandler handler : parameterHandlers) {
                    if (!foundHandlers.contains(handler) && handler.handles(parameter.getType(), value)) {
                        foundHandlers.add(handler);
                        parsedParameter++;
                    }
                }
            }
        }
        return foundHandlers;
    }

    private List<Object> extractParametersFromParsed(List<ParseResult> results) {
        List<Object> usableParameters = new ArrayList<>();
        boolean lastWasRemainder = false;
        for (ParseResult parseResult : results) {
            if (parseResult.getParameter().isRemainder() && !parseResult.getParameter().isListParam() && parseResult.getResult() instanceof String) {
                if (usableParameters.isEmpty() || !(usableParameters.get(usableParameters.size() - 1) instanceof String)) {
                    usableParameters.add(parseResult.getResult());
                } else if(lastWasRemainder){
                    int lastIndex = usableParameters.size() - 1;
                    usableParameters.set(lastIndex, usableParameters.get(lastIndex).toString() + " " + parseResult.getResult().toString());
                } else {
                    usableParameters.add(parseResult.getResult());
                }
            } else if (parseResult.getParameter().isListParam()) {
                if (usableParameters.isEmpty()) {
                    ArrayList<Object> list = new ArrayList<>();
                    list.add(parseResult.getResult().toString());
                    usableParameters.add(list);
                } else {
                    int lastIndex = usableParameters.size() - 1;
                    ((List) usableParameters.get(lastIndex)).add(parseResult.getResult());
                }
            } else {
                usableParameters.add(parseResult.getResult());
            }
            lastWasRemainder = parseResult.getParameter().isRemainder();
        }
        return usableParameters;
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(COMMANDS_PROCESSED_COUNTER, "Commands processed");
        metricService.registerCounter(COMMANDS_WRONG_PARAMETER_COUNTER, "Commands with incorrect parameter");
        this.parameterHandlers = parameterHandlers.stream().sorted(comparing(CommandParameterHandler::getPriority)).collect(Collectors.toList());
        if(commandAlternatives != null) {
            this.commandAlternatives = commandAlternatives.stream().sorted(comparing(Prioritized::getPriority)).collect(Collectors.toList());
        }
    }

    @Getter
    @Builder
    public static class CommandParseResult {
        private Parameters parameters;
        private Command command;
    }

    @Getter
    @Builder
    public static class UnParsedCommandResult {
        private UnParsedCommandParameter parameter;
        private Command command;
    }
}
