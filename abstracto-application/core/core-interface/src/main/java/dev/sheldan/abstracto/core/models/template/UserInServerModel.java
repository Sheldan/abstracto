package dev.sheldan.abstracto.core.models.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class UserInServerModel {
    private Long userInServerId;
    private UserModel user;
    private ServerModel server;
}
