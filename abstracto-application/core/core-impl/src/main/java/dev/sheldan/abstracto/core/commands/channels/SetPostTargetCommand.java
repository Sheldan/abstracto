package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Parameter;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.core.management.ChannelManagementService;
import dev.sheldan.abstracto.core.management.PostTargetManagement;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class SetPostTargetCommand implements Command {

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public Result execute(CommandContext commandContext) {
        GuildChannel channel = (GuildChannel) commandContext.getParameters().getParameters().get(1);
        String targetName = (String) commandContext.getParameters().getParameters().get(0);
        Guild guild = channel.getGuild();
        postTargetManagement.createOrUpdate(targetName, channel.getIdLong(), guild.getIdLong());
        log.info("Setting posttarget {} in {} to {}", targetName, guild.getIdLong(), channel.getId());
        return Result.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channel = Parameter.builder().name("channel").type(TextChannel.class).description("The channel to post towards").build();
        Parameter postTargetName = Parameter.builder().name("name").type(String.class).description("The name of the post target to redirect").build();
        List<Parameter> parameters = Arrays.asList(postTargetName, channel);
        return CommandConfiguration.builder()
                .name("posttarget")
                .module("channels")
                .parameters(parameters)
                .description("Sets the target of a post done by the bot")
                .causesReaction(true)
                .build();
    }
}
