package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AChannelType;
import dev.sheldan.abstracto.core.models.database.AServer;

import java.util.Optional;

public interface ChannelManagementService {
    Optional<AChannel> loadChannelOptional(Long id);
    AChannel loadChannel(Long id);
    AChannel createChannel(Long id, AChannelType type, AServer server);
    AChannel markAsDeleted(Long id);
    boolean channelExists(Long id);
    void removeChannel(Long id);
}
