package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.dto.ChannelDto;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.models.dto.UserDto;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AServerAChannelAUser {
    private ServerDto guild;
    private ChannelDto channel;
    private UserInServerDto aUserInAServer;
    private UserDto user;
}
