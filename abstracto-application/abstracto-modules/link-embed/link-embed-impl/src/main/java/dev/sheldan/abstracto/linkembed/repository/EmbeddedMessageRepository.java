package dev.sheldan.abstracto.linkembed.repository;


import dev.sheldan.abstracto.linkembed.model.database.EmbeddedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface EmbeddedMessageRepository extends JpaRepository<EmbeddedMessage, Long> {

    EmbeddedMessage findByEmbeddingMessageId(Long messageId);
    List<EmbeddedMessage> findByCreatedLessThan(Instant date);
    void deleteByEmbeddingMessageIdIn(List<Long> embeddedMessageId);
}
