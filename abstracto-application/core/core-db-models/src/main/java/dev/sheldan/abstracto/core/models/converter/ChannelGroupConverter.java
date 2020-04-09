package dev.sheldan.abstracto.core.models.converter;

import dev.sheldan.abstracto.core.models.AChannelGroup;
import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.dto.ChannelGroupDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelGroupConverter {

    @Autowired
    private ChannelConverter channelConverter;

    @Autowired
    private ServerConverter serverModelConverter;

    public ChannelGroupDto fromChannelGroup(AChannelGroup channelGroup) {
        ServerDto server = serverModelConverter.convertServer(channelGroup.getServer());
        return ChannelGroupDto
                .builder()
                .groupName(channelGroup.getGroupName())
                .server(server)
                .build();
    }

    public AChannelGroup fromChannelGroup(ChannelGroupDto channelGroup) {
        AServer server = serverModelConverter.fromDto(channelGroup.getServer());
        return AChannelGroup
                .builder()
                .groupName(channelGroup.getGroupName())
                .server(server)
                .build();
    }
}
