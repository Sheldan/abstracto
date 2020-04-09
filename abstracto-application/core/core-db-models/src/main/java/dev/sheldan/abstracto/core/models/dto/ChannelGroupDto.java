package dev.sheldan.abstracto.core.models.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ChannelGroupDto {

    private Long id;

    private String groupName;

    private ServerDto server;

    private List<ChannelDto> channels;
}
