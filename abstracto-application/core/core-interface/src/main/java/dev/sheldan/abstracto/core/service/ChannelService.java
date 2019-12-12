package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.AChannel;
import dev.sheldan.abstracto.core.models.AChannelType;

public interface ChannelService {
    AChannel loadChannel(Long id);
    AChannel createChannel(Long id, AChannelType type);
}
