package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.exception.ParameterTooLongException;
import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.Constants;
import dev.sheldan.abstracto.core.exception.MemberNotFoundException;
import dev.sheldan.abstracto.core.exception.RoleNotFoundInDBException;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.FullRole;
import dev.sheldan.abstracto.core.models.database.*;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.*;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.*;

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
            String contentStripped = event.getMessage().getContentStripped();
            List<String> parameters = Arrays.asList(contentStripped.split(" "));
            UnParsedCommandParameter unParsedParameter = new UnParsedCommandParameter(contentStripped);
            String commandName = commandManager.getCommandName(parameters.get(0), event.getGuild().getIdLong());
            foundCommand = commandManager.findCommandByParameters(commandName, unParsedParameter);
            tryToExecuteFoundCommand(event, userInitiatedContext, commandContextBuilder, foundCommand, unParsedParameter);

        } catch (Exception e) {
            log.error("Exception when preparing command.", e);
            CommandResult commandResult = CommandResult.fromError(e.getMessage(), e);
            CommandContext commandContext = commandContextBuilder.build();
            self.executePostCommandListener(null, commandContext, commandResult);
        }
    }

    private void tryToExecuteFoundCommand(@Nonnull MessageReceivedEvent event, UserInitiatedServerContext userInitiatedContext, CommandContext.CommandContextBuilder commandContextBuilder, Command foundCommand, UnParsedCommandParameter unParsedParameter) {
        try {
            Parameters parsedParameters = getParsedParameters(unParsedParameter, foundCommand, event.getMessage(), userInitiatedContext);
            CommandContext commandContext = commandContextBuilder.parameters(parsedParameters).build();
            ConditionResult conditionResult = commandService.isCommandExecutable(foundCommand, commandContext);
            CommandResult commandResult = null;
            if(conditionResult.isResult()) {
                if(foundCommand.getConfiguration().isAsync()) {
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

    @Transactional
    public void executePostCommandListener(Command foundCommand, CommandContext commandContext, CommandResult result) {
        for (PostCommandExecution postCommandExecution : executions) {
            postCommandExecution.execute(commandContext, result, foundCommand);
        }
    }

    @Transactional
    public CommandResult executeCommand(Command foundCommand, CommandContext commandContext) {
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

    public Parameters getParsedParameters(UnParsedCommandParameter unParsedCommandParameter, Command command, Message message, UserInitiatedServerContext userInitiatedServerContext){
        List<Object> parsedParameters = new ArrayList<>();
        if(command.getConfiguration().getParameters() == null || command.getConfiguration().getParameters().isEmpty()) {
            return Parameters.builder().parameters(parsedParameters).build();
        }
        Iterator<TextChannel> channelIterator = message.getMentionedChannels().iterator();
        Iterator<Emote> emoteIterator = message.getEmotesBag().iterator();
        Iterator<Member> memberIterator = message.getMentionedMembers().iterator();
        Iterator<Role> roleIterator = message.getMentionedRolesBag().iterator();
        Parameter param = command.getConfiguration().getParameters().get(0);
        boolean reminderActive = false;
        for (int i = 0; i < unParsedCommandParameter.getParameters().size(); i++) {
                if(i < command.getConfiguration().getParameters().size() && !param.isRemainder()) {
                    param = command.getConfiguration().getParameters().get(i);
                } else {
                    reminderActive = true;
                }
                String value = unParsedCommandParameter.getParameters().get(i);
                if(param.getMaxLength() != null && (value.length() + Constants.PARAMETER_LIMIT) > param.getMaxLength()) {
                    throw new ParameterTooLongException(command, param.getName(), value.length(), param.getMaxLength());
                }
                try {
                    if(param.getType().equals(Integer.class)){
                        parsedParameters.add(Integer.parseInt(value));
                    } else if(param.getType().equals(Double.class)){
                        parsedParameters.add(Double.parseDouble(value));
                    } else if(param.getType().equals(Long.class)){
                        parsedParameters.add(Long.parseLong(value));
                    } else if(param.getType().equals(TextChannel.class)){
                        parsedParameters.add(channelIterator.next());
                    } else if(param.getType().equals(Member.class)) {
                        if(StringUtils.isNumeric(value)) {
                            Member memberById = message.getGuild().getMemberById(Long.parseLong(value));
                            if(memberById == null) {
                                throw new MemberNotFoundException();
                            }
                            parsedParameters.add(memberById);
                        } else {
                            parsedParameters.add(memberIterator.next());
                        }
                    } else if(param.getType().equals(FullEmote.class)) {
                        // TODO maybe rework, this fails if two emotes are needed, and the second one is an emote, the first one a default one
                        // the second one shadows the first one, and there are too little parameters to go of
                        if (emoteIterator.hasNext()) {
                            try {
                                Long emoteId = Long.parseLong(value);
                                if(emoteManagementService.emoteExists(emoteId)) {
                                    AEmote aEmote = AEmote.builder().emoteId(emoteId).custom(true).build();
                                    FullEmote emote = FullEmote.builder().fakeEmote(aEmote).build();
                                    parsedParameters.add(emote);
                                }
                            } catch (Exception ex) {
                                Emote actualEmote = emoteIterator.next();
                                AEmote fakeEmote = emoteService.getFakeEmote(actualEmote);
                                FullEmote emote = FullEmote.builder().fakeEmote(fakeEmote).emote(actualEmote).build();
                                parsedParameters.add(emote);
                            }
                        } else {
                            try {
                                Long emoteId = Long.parseLong(value);
                                if(emoteManagementService.emoteExists(emoteId)) {
                                    // we do not need to load the actual emote, as there is no guarantee that it exists anyway
                                    // there might be multiple emotes with the same emoteId, so we dont have any gain to fetch any of them
                                    AEmote aEmote = AEmote.builder().emoteId(emoteId).custom(true).build();
                                    FullEmote emote = FullEmote.builder().fakeEmote(aEmote).build();
                                    parsedParameters.add(emote);
                                }
                            } catch (Exception ex) {
                                AEmote fakeEmote = emoteService.getFakeEmote(value);
                                FullEmote emote = FullEmote.builder().fakeEmote(fakeEmote).build();
                                parsedParameters.add(emote);
                            }
                        }
                    } else if(param.getType().equals(AEmote.class)) {
                        // TODO maybe rework, this fails if two emotes are needed, and the second one is an emote, the first one a default one
                        // the second one shadows the first one, and there are too little parameters to go of
                        if (emoteIterator.hasNext()) {
                            parsedParameters.add(emoteService.getFakeEmote(emoteIterator.next()));
                        } else {
                            parsedParameters.add(emoteService.getFakeEmote(value));
                        }
                    } else if(CommandParameterKey.class.isAssignableFrom(param.getType())) {
                        CommandParameterKey cast = (CommandParameterKey) CommandParameterKey.getEnumFromKey(param.getType(), value);
                        parsedParameters.add(cast);
                    } else if(param.getType().equals(FullRole.class)) {
                        ARole aRole;
                        if(StringUtils.isNumeric(value)) {
                            long roleId = Long.parseLong(value);
                            aRole = roleManagementService.findRoleOptional(roleId).orElseThrow(() -> new RoleNotFoundInDBException(roleId));
                        } else {
                            long roleId = roleIterator.next().getIdLong();
                            aRole = roleManagementService.findRoleOptional(roleId).orElseThrow(() -> new RoleNotFoundInDBException(roleId));
                        }
                        Role role = roleService.getRoleFromGuild(aRole);
                        FullRole fullRole = FullRole.builder().role(aRole).serverRole(role).build();
                        parsedParameters.add(fullRole);
                    } else if(param.getType().equals(ARole.class)) {
                        if(StringUtils.isNumeric(value)) {
                            long roleId = Long.parseLong(value);
                            parsedParameters.add(roleManagementService.findRoleOptional(roleId).orElseThrow(() -> new RoleNotFoundInDBException(roleId)));
                        } else {
                            long roleId = roleIterator.next().getIdLong();
                            parsedParameters.add(roleManagementService.findRoleOptional(roleId).orElseThrow(() -> new RoleNotFoundInDBException(roleId)));
                        }
                    } else if(param.getType().equals(Boolean.class)) {
                        parsedParameters.add(Boolean.valueOf(value));
                    } else if (param.getType().equals(Duration.class)) {
                        parsedParameters.add(ParseUtils.parseDuration(value));
                    } else {
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
                } catch (NoSuchElementException e) {
                    throw new IncorrectParameterException(command, param.getType(), param.getName());
                } catch (IllegalArgumentException e) {

                }
            }

        return Parameters.builder().parameters(parsedParameters).build();
    }
}
