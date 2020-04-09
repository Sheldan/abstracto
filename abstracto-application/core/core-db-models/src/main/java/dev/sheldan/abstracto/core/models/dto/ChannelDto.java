package dev.sheldan.abstracto.core.models.dto;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ChannelDto implements SnowFlake {
    public Long id;
    private Boolean deleted;
    private ServerDto server;
}
