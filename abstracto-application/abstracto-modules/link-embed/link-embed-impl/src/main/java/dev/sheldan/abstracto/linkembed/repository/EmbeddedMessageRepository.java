package dev.sheldan.abstracto.linkembed.repository;


import dev.sheldan.abstracto.linkembed.model.database.EmbeddedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmbeddedMessageRepository extends JpaRepository<EmbeddedMessage, Long> {

    EmbeddedMessage findByEmbeddingMessageId(Long messageId);
}
