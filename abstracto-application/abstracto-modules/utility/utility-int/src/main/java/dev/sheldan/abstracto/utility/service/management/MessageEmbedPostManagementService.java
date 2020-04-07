package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.EmbeddedMessage;
import net.dv8tion.jda.api.entities.Message;

import java.util.Optional;

public interface MessageEmbedPostManagementService {
    void createMessageEmbed(CachedMessage embeddedMessage, Message messageContainingEmbed, AUserInAServer cause);
    Optional<EmbeddedMessage> findEmbeddedPostByMessageId(Long messageId);
    void deleteEmbeddedMessage(EmbeddedMessage embeddedMessage);
    void deleteEmbeddedMessageTransactional(EmbeddedMessage embeddedMessage);
}
