package dev.sheldan.abstracto.core.models.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class ChannelGroupCommandDto {
    private Long commandInGroupId;
    private CommandDto command;
    private ChannelGroupDto group;
    private Boolean enabled;
}
