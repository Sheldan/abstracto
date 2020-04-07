package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.condition.ConditionalCommand;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.exception.IncorrectParameter;
import dev.sheldan.abstracto.core.command.exception.ParameterTooLong;
import dev.sheldan.abstracto.core.command.service.ChannelGroupCommandService;
import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.Constants;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
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
    private UserManagementService userManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    @Lazy
    private CommandReceivedHandler self;

    @Autowired
    private ChannelGroupCommandService channelGroupCommandService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Override
    @Async
    @Transactional
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if(!commandManager.isCommand(event.getMessage())) {
            return;
        }
        if(!event.isFromGuild()) {
            return;
        }
        CommandContext.CommandContextBuilder commandContextBuilder = CommandContext.builder()
                .author(event.getMember())
                .guild(event.getGuild())
                .channel(event.getTextChannel())
                .message(event.getMessage())
                .jda(event.getJDA())
                .userInitiatedContext(buildTemplateParameter(event));
        Command foundCommand = null;
        try {
            String contentStripped = event.getMessage().getContentStripped();
            List<String> parameters = Arrays.asList(contentStripped.split(" "));
            UnParsedCommandParameter unparsedParameter = new UnParsedCommandParameter(contentStripped);
            String commandName = parameters.get(0).substring(1);
            foundCommand = commandManager.findCommandByParameters(commandName, unparsedParameter);
            Parameters parsedParameters = getParsedParameters(unparsedParameter, foundCommand, event.getMessage());
            CommandContext commandContext = commandContextBuilder.parameters(parsedParameters).build();
            if(foundCommand instanceof ConditionalCommand) {
                ConditionalCommand castedCommand = (ConditionalCommand) foundCommand;
                ConditionResult conditionResult = checkConditions(commandContext, foundCommand, castedCommand.getConditions());
                if(!conditionResult.isResult()) {
                    throw new AbstractoRunTimeException(conditionResult.getReason());
                }
            }
            CommandResult commandResult = self.executeCommand(foundCommand, commandContext);
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

    public ConditionResult checkConditions(CommandContext commandContext, Command command, List<CommandCondition> conditions) {
        if(conditions != null) {
            for (CommandCondition condition : conditions) {
                ConditionResult conditionResult = condition.shouldExecute(commandContext, command);
                if(!conditionResult.isResult()) {
                    return conditionResult;
                }
            }
        }
        return ConditionResult.builder().result(true).build();
    }

    private UserInitiatedServerContext buildTemplateParameter(MessageReceivedEvent event) {
        AChannel channel = channelManagementService.loadChannel(event.getChannel().getIdLong());
        AServer server = serverManagementService.loadOrCreate(event.getGuild().getIdLong());
        AUserInAServer user = userManagementService.loadUser(event.getMember());
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
        if(command.getConfiguration().getParameters() == null || command.getConfiguration().getParameters().size() == 0) {
            return Parameters.builder().parameters(parsedParameters).build();
        }
        Iterator<TextChannel> channelIterator = message.getMentionedChannels().iterator();
        Iterator<Emote> emoteIterator = message.getEmotes().iterator();
        Iterator<Member> memberIterator = message.getMentionedMembers().iterator();
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
                    } else if(param.getType().equals(Boolean.class)) {
                        parsedParameters.add(Boolean.valueOf(value));
                    } else if (param.getType().equals(Duration.class)) {
                        parsedParameters.add(ParseUtils.parseDuration(value));
                    } else {
                        if(!reminderActive) {
                            parsedParameters.add(value);
                        } else {
                            if(parsedParameters.size() == 0) {
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
