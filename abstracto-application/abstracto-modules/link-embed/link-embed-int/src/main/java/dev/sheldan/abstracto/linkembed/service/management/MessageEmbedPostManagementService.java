package dev.sheldan.abstracto.linkembed.service.management;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.linkembed.model.database.EmbeddedMessage;
import net.dv8tion.jda.api.entities.Message;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageEmbedPostManagementService {
    void createMessageEmbed(CachedMessage embeddedMessage, Message messageContainingEmbed, AUserInAServer cause, String deletionButtonId);
    Optional<EmbeddedMessage> findEmbeddedPostByMessageId(Long messageId);
    void deleteEmbeddedMessage(EmbeddedMessage embeddedMessage);
    List<EmbeddedMessage> getEmbeddedMessagesOlderThan(Instant date);
    void deleteEmbeddedMessagesViaId(List<Long> embeddingMessageId);
}
