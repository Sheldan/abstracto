package dev.sheldan.abstracto.command.channels;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.Configuration;
import dev.sheldan.abstracto.command.execution.Context;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import net.dv8tion.jda.api.entities.GuildChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PostTarget implements Command {

    @Autowired
    private PostTargetService service;

    @Autowired
    private ChannelService channelService;

    @Override
    public Result execute(Context context) {
        GuildChannel channel = (GuildChannel) context.getParameters().getParameters().get(1);
        String targetName = (String) context.getParameters().getParameters().get(0);
        AChannel dbChannel = channelService.loadChannel(channel.getIdLong());
        service.createOrUpdate(targetName, dbChannel);
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
