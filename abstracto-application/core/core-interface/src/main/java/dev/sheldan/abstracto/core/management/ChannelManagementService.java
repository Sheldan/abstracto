package dev.sheldan.abstracto.core.management;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AChannelType;

public interface ChannelManagementService {
    AChannel loadChannel(Long id);
    AChannel createChannel(Long id, AChannelType type);
}
