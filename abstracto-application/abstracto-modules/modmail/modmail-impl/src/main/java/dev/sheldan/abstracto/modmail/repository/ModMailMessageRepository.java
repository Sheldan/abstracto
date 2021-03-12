package dev.sheldan.abstracto.modmail.repository;

import dev.sheldan.abstracto.modmail.model.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository to manage the stored {@link ModMailMessage} instances
 */
@Repository
public interface ModMailMessageRepository extends JpaRepository<ModMailMessage, Long> {
    List<ModMailMessage> findByThreadReference(ModMailThread modMailThread);

    Optional<ModMailMessage> findByMessageId(Long messageId);
}
