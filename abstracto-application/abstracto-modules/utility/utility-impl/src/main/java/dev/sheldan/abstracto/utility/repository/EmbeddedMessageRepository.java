package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.utility.models.EmbeddedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbeddedMessageRepository extends JpaRepository<EmbeddedMessage, Long> {
    EmbeddedMessage findByEmbeddingMessageId(Long messageId);
}
