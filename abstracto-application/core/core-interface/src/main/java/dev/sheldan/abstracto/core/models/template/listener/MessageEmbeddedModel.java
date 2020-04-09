package dev.sheldan.abstracto.core.models.template.listener;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.template.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

@Getter
@Setter
@SuperBuilder
public class MessageEmbeddedModel extends UserInitiatedServerContextModel {
    private CachedMessage embeddedMessage;
    private ChannelModel sourceChannel;
    private ServerModel sourceServer;
    private UserModel embeddedUser;
    private UserInServerModel embeddedUserInServer;
}
