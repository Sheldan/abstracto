package dev.sheldan.abstracto.core.models.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserInServerDto {
    private Long userInServerId;

    private UserDto user;

    private ServerDto server;
}
