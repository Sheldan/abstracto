package dev.sheldan.abstracto.core.management;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.AChannelType;

public interface ChannelManagementService {
    AChannel loadChannel(Long id);
    AChannel createChannel(Long id, AChannelType type);
    void markAsDeleted(Long id);
    void removeChannel(Long id);
}
