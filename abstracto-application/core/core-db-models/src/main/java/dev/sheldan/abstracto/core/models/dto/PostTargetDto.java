package dev.sheldan.abstracto.core.models.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PostTargetDto {
    private Long id;
    private String name;
    private ChannelDto channelReference;
    private ServerDto serverReference;
}
