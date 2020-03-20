package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AChannel;

public interface ChannelService {
    void sendTextInAChannel(String text, AChannel channel);
}
