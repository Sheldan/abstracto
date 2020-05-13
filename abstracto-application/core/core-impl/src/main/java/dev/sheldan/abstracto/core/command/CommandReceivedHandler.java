package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.ParameterTooLong;
import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.Constants;
import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.exception.RoleNotFoundInDBException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
                .channel(event.getTextChannel())
                .message(event.getMessage())
                .jda(event.getJDA())
                .userInitiatedContext(userInitiatedContext);
        Command foundCommand = null;
        try {
            String contentStripped = event.getMessage().getContentStripped();
            List<String> parameters = Arrays.asList(contentStripped.split(" "));
            UnParsedCommandParameter unparsedParameter = new UnParsedCommandParameter(contentStripped);
            String commandName = commandManager.getCommandName(parameters.get(0), event.getGuild().getIdLong());
            foundCommand = commandManager.findCommandByParameters(commandName, unparsedParameter);
            Parameters parsedParameters = getParsedParameters(unparsedParameter, foundCommand, event.getMessage(), userInitiatedContext);
            CommandContext commandContext = commandContextBuilder.parameters(parsedParameters).build();
            ConditionResult conditionResult = commandService.isCommandExecutable(foundCommand, commandContext);
            CommandResult commandResult;
            if(conditionResult.isResult()) {
                commandResult = self.executeCommand(foundCommand, commandContext);
            } else {
                commandResult = CommandResult.fromCondition(conditionResult);
            }
            for (PostCommandExecution postCommandExecution : executions) {
                postCommandExecution.execute(commandContext, commandResult, foundCommand);
            }
        } catch (Exception e) {
            CommandResult commandResult = CommandResult.fromError(e.getMessage(), e);
            CommandContext commandContext = commandContextBuilder.build();
            for (PostCommandExecution postCommandExecution : executions) {
                postCommandExecution.execute(commandContext, commandResult, foundCommand);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CommandResult executeCommand(Command foundCommand, CommandContext commandContext) {
        return foundCommand.execute(commandContext);
    }

    private UserInitiatedServerContext buildTemplateParameter(MessageReceivedEvent event) {
        Optional<AChannel> channel = channelManagementService.loadChannel(event.getChannel().getIdLong());
        AServer server = serverManagementService.loadOrCreate(event.getGuild().getIdLong());
        AUserInAServer user = userInServerManagementService.loadUser(event.getMember());
        AChannel channel1 = channel.orElseThrow(() -> new ChannelNotFoundException(event.getChannel().getIdLong(), event.getGuild().getIdLong()));
        return UserInitiatedServerContext
                .builder()
                .channel(channel1)
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
        Iterator<Emote> emoteIterator = message.getEmotes().iterator();
        Iterator<Member> memberIterator = message.getMentionedMembers().iterator();
        Iterator<Role> roleIterator = message.getMentionedRoles().iterator();
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
                    throw new ParameterTooLong("The passed parameter was too long.", command, param.getName(), value.length(), param.getMaxLength());
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
                        parsedParameters.add(memberIterator.next());
                    } else if(param.getType().equals(Emote.class)) {
                        // TODO maybe rework, this fails if two emotes are needed, and the second one is an emote, the first one a default one
                        // the second one shadows the first one, and there are too little parameters to go of
                        if (emoteIterator.hasNext()) {
                            parsedParameters.add(emoteIterator.next());
                        } else {
                            parsedParameters.add(value);
                        }
                    } else if(param.getType().equals(ARole.class)) {
                        if(StringUtils.isNumeric(value)) {
                            long roleId = Long.parseLong(value);
                            parsedParameters.add(roleManagementService.findRole(roleId, userInitiatedServerContext.getServer()).orElseThrow(() -> new RoleNotFoundInDBException(roleId, message.getGuild().getIdLong())));
                        } else {
                            long roleId = roleIterator.next().getIdLong();
                            parsedParameters.add(roleManagementService.findRole(roleId, userInitiatedServerContext.getServer()).orElseThrow(() -> new RoleNotFoundInDBException(roleId, message.getGuild().getIdLong())));
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
                    throw new IncorrectParameter("The passed parameters did not have the correct type.", command, param.getType(), param.getName());
                }
            }

        return Parameters.builder().parameters(parsedParameters).build();
    }
}
