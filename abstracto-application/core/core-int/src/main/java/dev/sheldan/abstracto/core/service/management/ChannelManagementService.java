package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelType;
import dev.sheldan.abstracto.core.models.database.AServer;
import net.dv8tion.jda.api.entities.Channel;

import java.util.Optional;

public interface ChannelManagementService {
    Optional<AChannel> loadChannelOptional(Long id);
    AChannel loadChannel(Long id);
    AChannel loadChannel(Channel textChannel);
    AChannel createChannel(Long id, AChannelType type, AServer server);
    AChannel createThread(Long id, AChannelType type, AServer server, AChannel parentChannel);
    AChannel markAsDeleted(Long id);
    boolean channelExists(Long id);
    void removeChannel(Long id);
}
