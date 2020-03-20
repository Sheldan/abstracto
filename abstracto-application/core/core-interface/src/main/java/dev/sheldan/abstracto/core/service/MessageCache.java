package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Message;

public interface MessageCache {
    Message putMessageInCache(Message message);
    Message getMessageFromCache(Message message);
    Message getMessageFromCache(Long messageId, Long textChannelId, Long guildId);
}
