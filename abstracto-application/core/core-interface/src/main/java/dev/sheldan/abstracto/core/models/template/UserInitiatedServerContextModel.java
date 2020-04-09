package dev.sheldan.abstracto.core.models.template;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @NoArgsConstructor
@Setter
@SuperBuilder
public class UserInitiatedServerContextModel extends ServerContextModel {
    private ChannelModel channel;
    private UserModel userModel;
    private UserInServerModel userInServer;

}
