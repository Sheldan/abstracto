package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

public interface MessageService {
    void addReactionToMessage(String emoteKey, Long serverId, Message message);
    CompletableFuture<Void> deleteMessageInChannelInServer(Long serverId, Long channelId, Long messageId);
}
