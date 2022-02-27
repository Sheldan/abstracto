package dev.sheldan.abstracto.repostdetection.converter;

import dev.sheldan.abstracto.core.models.FullChannel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.repostdetection.model.database.RepostCheckChannelGroup;
import dev.sheldan.abstracto.repostdetection.model.template.RepostCheckChannelGroupDisplayModel;
import dev.sheldan.abstracto.repostdetection.model.template.RepostCheckChannelsModel;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RepostCheckChannelModelConverter {

    @Autowired
    private ChannelService channelService;

    public RepostCheckChannelsModel fromRepostCheckChannelGroups(List<RepostCheckChannelGroup> channelGroups, Guild guild) {
        List<RepostCheckChannelGroupDisplayModel> repostCheckChannelGroups = new ArrayList<>();
        channelGroups.forEach(repostCheckChannelGroup -> {
            List<FullChannel> fullChannels = repostCheckChannelGroup.getChannelGroup().getChannels().stream().map(channel ->
                FullChannel
                        .builder()
                        .channel(channel)
                        .serverChannel(channelService.getMessageChannelFromServerNullable(guild, channel.getId()))
                        .build()
            ).collect(Collectors.toList());
            repostCheckChannelGroups.add(
                    RepostCheckChannelGroupDisplayModel
                    .builder()
                    .channelGroup(repostCheckChannelGroup)
                            .channels(fullChannels)
                            .build());
        });
        return RepostCheckChannelsModel.builder().repostCheckChannelGroups(repostCheckChannelGroups).build();
    }

}
