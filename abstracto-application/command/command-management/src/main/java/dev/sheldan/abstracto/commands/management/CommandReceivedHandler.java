package dev.sheldan.abstracto.commands.management;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.PostCommandExecution;
import dev.sheldan.abstracto.command.execution.*;
import dev.sheldan.abstracto.command.meta.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AServer;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CommandReceivedHandler extends ListenerAdapter {

    @Autowired
    private CommandManager manager;

    @Autowired
    private PostCommandExecution execution;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if(!manager.isCommand(event.getMessage())) {
            return;
        }
        List<String> parameters = Arrays.asList(event.getMessage().getContentStripped().split(" "));
        UnParsedCommandParameter unparsedParameter = new UnParsedCommandParameter();
        unparsedParameter.setParameters(parameters.subList(1, parameters.size()));
        String withoutPrefix = parameters.get(0).substring(1);
        Command foundCommand = manager.findCommandByParameters(withoutPrefix, unparsedParameter);
        Parameters parsedParameters = getParsedParameters(unparsedParameter, foundCommand, event.getMessage());
        CommandContext commandContext = CommandContext.builder()
                .author(event.getAuthor())
                .guild(event.getGuild())
                .channel(event.getTextChannel())
                .message(event.getMessage())
                .parameters(parsedParameters)
                .jda(event.getJDA())
                .commandTemplateContext(buildTemplateParameter(event))
                .build();
        Result result = foundCommand.execute(commandContext);
        execution.execute(commandContext, result, foundCommand);
    }

    private CommandTemplateContext buildTemplateParameter(MessageReceivedEvent event) {
        AChannel channel = channelManagementService.loadChannel(event.getChannel().getIdLong());
        AServer server = serverManagementService.loadServer(event.getGuild().getIdLong());
        return CommandTemplateContext.builder().channel(channel).server(server).build();
    }

    public Parameters getParsedParameters(UnParsedCommandParameter unParsedCommandParameter, Command command, Message message){
        List<Object> parsedParameters = new ArrayList<>();
        int mentionedChannelsCount = 0;
        for (int i = 0; i < unParsedCommandParameter.getParameters().size(); i++) {
            Parameter param = command.getConfiguration().getParameters().get(i);
            String value = unParsedCommandParameter.getParameters().get(i);
            if(param.getType().equals(Integer.class)){
                parsedParameters.add(Integer.parseInt(value));
            } else if(param.getType().equals(Double.class)){
                parsedParameters.add(Double.parseDouble(value));
            } else if(param.getType().equals(GuildChannel.class)){
                parsedParameters.add(message.getMentionedChannels().get(mentionedChannelsCount));
                mentionedChannelsCount++;
            } else{
                parsedParameters.add(value);
            }
        }
        return Parameters.builder().parameters(parsedParameters).build();
    }
}
