package dev.sheldan.abstracto.commands.management;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.PostCommandExecution;
import dev.sheldan.abstracto.command.execution.*;
import dev.sheldan.abstracto.command.meta.UnParsedCommandParameter;
import dev.sheldan.abstracto.commands.management.exception.IncorrectParameterException;
import dev.sheldan.abstracto.commands.management.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.*;

@Service
public class CommandReceivedHandler extends ListenerAdapter {

    @Autowired
    private CommandManager commandManager;

    @Autowired
    private List<PostCommandExecution> executions;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
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
                .commandTemplateContext(buildTemplateParameter(event));
        Command foundCommand = null;
        try {
            List<String> parameters = Arrays.asList(event.getMessage().getContentStripped().split(" "));
            UnParsedCommandParameter unparsedParameter = new UnParsedCommandParameter();
            unparsedParameter.setParameters(parameters.subList(1, parameters.size()));
            String withoutPrefix = parameters.get(0).substring(1);
            foundCommand = commandManager.findCommandByParameters(withoutPrefix, unparsedParameter);
            Parameters parsedParameters = getParsedParameters(unparsedParameter, foundCommand, event.getMessage());
            CommandContext commandContext = commandContextBuilder.parameters(parsedParameters).build();
            Result result = foundCommand.execute(commandContext);
            for (PostCommandExecution postCommandExecution : executions) {
                postCommandExecution.execute(commandContext, result, foundCommand);
            }
        } catch (Exception e) {
            Result result = Result.fromError(e.getMessage(), e);
            CommandContext commandContext = commandContextBuilder.build();
            for (PostCommandExecution postCommandExecution : executions) {
                postCommandExecution.execute(commandContext, result, foundCommand);
            }
        }

    }

    private CommandTemplateContext buildTemplateParameter(MessageReceivedEvent event) {
        AChannel channel = channelManagementService.loadChannel(event.getChannel().getIdLong());
        AServer server = serverManagementService.loadServer(event.getGuild().getIdLong());
        return CommandTemplateContext.builder().channel(channel).server(server).build();
    }

    public Parameters getParsedParameters(UnParsedCommandParameter unParsedCommandParameter, Command command, Message message){
        List<Object> parsedParameters = new ArrayList<>();
        Iterator<TextChannel> channelIterator = message.getMentionedChannels().iterator();
        Iterator<Member> memberIterator = message.getMentionedMembers().iterator();
            for (int i = 0; i < unParsedCommandParameter.getParameters().size(); i++) {
                Parameter param = command.getConfiguration().getParameters().get(i);
                String value = unParsedCommandParameter.getParameters().get(i);
                try {
                    if(param.getType().equals(Integer.class)){
                        parsedParameters.add(Integer.parseInt(value));
                    } else if(param.getType().equals(Double.class)){
                        parsedParameters.add(Double.parseDouble(value));
                    }  else if(param.getType().equals(Long.class)){
                        parsedParameters.add(Long.parseLong(value));
                    } else if(param.getType().equals(TextChannel.class)){
                        parsedParameters.add(channelIterator.next());
                    } else if(param.getType().equals(Member.class)) {
                        parsedParameters.add(memberIterator.next());
                    } else {
                        parsedParameters.add(value);
                    }
                } catch (NoSuchElementException e) {
                    throw new IncorrectParameterException("The passed parameters did not have the correct type.", command, param.getType(), param.getName());
                }
            }

        return Parameters.builder().parameters(parsedParameters).build();
    }
}
