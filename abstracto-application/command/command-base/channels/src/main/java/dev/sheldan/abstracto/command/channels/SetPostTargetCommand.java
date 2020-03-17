package dev.sheldan.abstracto.command.channels;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.management.PostTargetManagement;
import net.dv8tion.jda.api.entities.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class SetPostTargetCommand implements Command {

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    @Transactional
    public Result execute(CommandContext commandContext) {
        GuildChannel channel = (GuildChannel) commandContext.getParameters().getParameters().get(1);
        String targetName = (String) commandContext.getParameters().getParameters().get(0);
        postTargetManagement.createOrUpdate(targetName, channel.getIdLong(), channel.getGuild().getIdLong());
        return Result.fromSuccess();
    }

    @Override
    public Configuration getConfiguration() {
        Parameter channel = Parameter.builder().name("channel").type(GuildChannel.class).description("The channel to post towards").build();
        Parameter postTargetName = Parameter.builder().name("name").type(String.class).description("The name of the post target to redirect").build();
        List<Parameter> parameters = Arrays.asList(postTargetName, channel);
        return Configuration.builder()
                .name("posttarget")
                .module("channels")
                .parameters(parameters)
                .description("Sets the target of a post done by the bot")
                .causesReaction(false)
                .build();
    }
}
