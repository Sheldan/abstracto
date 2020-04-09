package dev.sheldan.abstracto.core.models.converter;

import dev.sheldan.abstracto.core.models.PostTarget;
import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.PostTargetDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PostTargetConverter {

    @Autowired
    private ChannelConverter channelConverter;

    @Autowired
    private ServerConverter serverConverter;

    public PostTargetDto fromPostTarget(PostTarget postTarget) {
        ServerDto convertedServer = serverConverter.convertServer(postTarget.getServerReference());
        ChannelDto convertedChannel = channelConverter.fromChannel(postTarget.getChannelReference());
        return PostTargetDto
                .builder()
                .id(postTarget.getId())
                .name(postTarget.getName())
                .serverReference(convertedServer)
                .channelReference(convertedChannel)
                .build();
    }
}
