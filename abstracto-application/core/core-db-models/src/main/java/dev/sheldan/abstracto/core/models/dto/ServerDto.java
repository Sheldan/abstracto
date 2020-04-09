package dev.sheldan.abstracto.core.models.dto;


import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ServerDto implements SnowFlake {
    private Long id;
    private String name;
    private List<ChannelDto> channels;
    private List<UserInServerDto> users;
    private List<RoleDto> roles;
}
