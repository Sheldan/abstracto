package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.exception.CommandParameterValidationException;
import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.handler.CommandParameterHandler;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.*;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.*;
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

    @Override
    @Transactional
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if(!event.isFromGuild()) {
            return;
        }
        if(!commandManager.isCommand(event.getMessage())) {
            return;
        }
        UserInitiatedServerContext userInitiatedContext = buildTemplateParameter(event);
        CommandContext.CommandContextBuilder commandContextBuilder = CommandContext.builder()
                .author(event.getMember())
                .guild(event.getGuild())
                .undoActions(new ArrayList<>())
                .channel(event.getTextChannel())
                .message(event.getMessage())
                .jda(event.getJDA())
                .userInitiatedContext(userInitiatedContext);
        final Command foundCommand;
        try {
            String contentStripped = event.getMessage().getContentRaw();
            List<String> parameters = Arrays.asList(contentStripped.split(" "));
            UnParsedCommandParameter unParsedParameter = new UnParsedCommandParameter(contentStripped);
            String commandName = commandManager.getCommandName(parameters.get(0), event.getGuild().getIdLong());
            foundCommand = commandManager.findCommandByParameters(commandName, unParsedParameter);
            tryToExecuteFoundCommand(event, commandContextBuilder, foundCommand, unParsedParameter);

        } catch (Exception e) {
            log.error("Exception when executing command.", e);
            CommandResult commandResult = CommandResult.fromError(e.getMessage(), e);
            CommandContext commandContext = commandContextBuilder.build();
            self.executePostCommandListener(null, commandContext, commandResult);
        }
    }

    private void tryToExecuteFoundCommand(@Nonnull MessageReceivedEvent event, CommandContext.CommandContextBuilder commandContextBuilder, Command foundCommand, UnParsedCommandParameter unParsedParameter) {
        try {
            Parameters parsedParameters = getParsedParameters(unParsedParameter, foundCommand, event.getMessage());
            validateCommandParameters(parsedParameters, foundCommand);
            CommandContext commandContext = commandContextBuilder.parameters(parsedParameters).build();
            ConditionResult conditionResult = commandService.isCommandExecutable(foundCommand, commandContext);
            CommandResult commandResult = null;
            if(conditionResult.isResult()) {
               if(foundCommand.getConfiguration().isAsync()) {
                   log.info("Executing async command {} for server {} in channel {} based on message {} by user {}.",
                           foundCommand.getConfiguration().getName(), commandContext.getGuild().getId(), commandContext.getChannel().getId(), commandContext.getMessage().getId(), commandContext.getAuthor().getId());

                   foundCommand.executeAsync(commandContext).thenAccept(result ->
                        executePostCommandListener(foundCommand, commandContext, result)
                    ).exceptionally(throwable -> {
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
                // TODO can it be done nicer?
                if(conditionResult.getException() != null) {
                    throw conditionResult.getException();
                }
            }
            if(commandResult != null) {
                self.executePostCommandListener(foundCommand, commandContext, commandResult);
            }
        } catch (Exception e) {
            log.error("Exception when executing command.", e);
            CommandResult commandResult = CommandResult.fromError(e.getMessage(), e);
            CommandContext commandContext = commandContextBuilder.build();
            self.executePostCommandListener(foundCommand, commandContext, commandResult);
        }
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
                    throw new CommandParameterValidationException(parameterValidator.getParameters(), parameterValidator.getTemplateName(), parameter);
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

    @Transactional
    public CommandResult executeCommand(Command foundCommand, CommandContext commandContext) {
        log.info("Executing sync command {} for server {} in channel {} based on message {} by user {}.",
                foundCommand.getConfiguration().getName(), commandContext.getGuild().getId(), commandContext.getChannel().getId(), commandContext.getMessage().getId(), commandContext.getAuthor().getId());
        return foundCommand.execute(commandContext);
    }

    private UserInitiatedServerContext buildTemplateParameter(MessageReceivedEvent event) {
        AChannel channel = channelManagementService.loadChannel(event.getChannel().getIdLong());
        AServer server = serverManagementService.loadOrCreate(event.getGuild().getIdLong());
        AUserInAServer user = userInServerManagementService.loadUser(event.getMember());
        return UserInitiatedServerContext
                .builder()
                .channel(channel)
                .server(server)
                .member(event.getMember())
                .aUserInAServer(user)
                .user(user.getUserReference())
                .messageChannel(event.getTextChannel())
                .guild(event.getGuild())
                .build();
    }

    public Parameters getParsedParameters(UnParsedCommandParameter unParsedCommandParameter, Command command, Message message){
        List<Object> parsedParameters = new ArrayList<>();
        if(command.getConfiguration().getParameters() == null || command.getConfiguration().getParameters().isEmpty()) {
            return Parameters.builder().parameters(parsedParameters).build();
        }
        log.trace("Parsing parameters for command {} based on message {}.", command.getConfiguration().getName(), message.getId());
        Iterator<TextChannel> channelIterator = message.getMentionedChannels().iterator();
        Iterator<Emote> emoteIterator = message.getEmotesBag().iterator();
        Iterator<Member> memberIterator = message.getMentionedMembers().iterator();
        Iterator<Role> roleIterator = message.getMentionedRolesBag().iterator();
        Parameter param = command.getConfiguration().getParameters().get(0);
        CommandParameterIterators iterators = new CommandParameterIterators(channelIterator, emoteIterator, memberIterator, roleIterator);
        boolean reminderActive = false;
        List<CommandParameterHandler> orderedHandlers = parameterHandlers.stream().sorted(comparing(CommandParameterHandler::getPriority)).collect(Collectors.toList());
        for (int i = 0; i < unParsedCommandParameter.getParameters().size(); i++) {
            if(i < command.getConfiguration().getParameters().size() && !param.isRemainder()) {
                param = command.getConfiguration().getParameters().get(i);
            } else {
                reminderActive = true;
            }
            String value = unParsedCommandParameter.getParameters().get(i);
            boolean handlerMatched = false;
            for (CommandParameterHandler handler : orderedHandlers) {
                try {
                    if(handler.handles(param.getType())) {
                        handlerMatched = true;
                        parsedParameters.add(handler.handle(value, iterators, param.getType(), message));
                        break;
                    }
                } catch (NoSuchElementException e) {
                    throw new IncorrectParameterException(command, param.getType(), param.getName());
                }
            }
            if(!handlerMatched) {
                if(!reminderActive) {
                    parsedParameters.add(value);
                } else {
                    if(parsedParameters.isEmpty()) {
                        parsedParameters.add(value);
                    } else {
                        int lastIndex = parsedParameters.size() - 1;
                        parsedParameters.set(lastIndex, parsedParameters.get(lastIndex) + " " + value);
                    }
                }
            }

        }

        return Parameters.builder().parameters(parsedParameters).build();
    }
}
