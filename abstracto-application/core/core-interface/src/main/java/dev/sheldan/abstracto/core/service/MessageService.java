package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MessageService {
    void addReactionToMessage(String emoteKey, Long serverId, Message message);
    CompletableFuture<Void> deleteMessageInChannelInServer(Long serverId, Long channelId, Long messageId);
    CompletableFuture<Message> createStatusMessage(MessageToSend messageToSend, AChannel channel);
    void updateStatusMessage(AChannel channel, Long messageId, MessageToSend messageToSend);
}
