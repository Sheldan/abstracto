package dev.sheldan.abstracto.core.converter;

import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.template.ChannelModel;
import org.springframework.stereotype.Component;

@Component
public class ChannelModelConverter {
    public ChannelModel fromChanel(ChannelDto channelDto) {
        return ChannelModel
                .builder()
                .id(channelDto.getId())
                .build();
    }
}
