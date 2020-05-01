package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.utility.models.database.EmbeddedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

public interface EmbeddedMessageRepository extends JpaRepository<EmbeddedMessage, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    EmbeddedMessage findByEmbeddingMessageId(Long messageId);
}
