package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AServerAChannelAUser {
    private AServer guild;
    private AChannel channel;
    private AUserInAServer aUserInAServer;
    private AUser user;
}
