package dev.sheldan.abstracto.modmail.repository;

import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * Repository to manage the stored {@link ModMailMessage} instances
 */
@Repository
public interface ModMailMessageRepository extends JpaRepository<ModMailMessage, Long> {
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<ModMailMessage> findByThreadReference(ModMailThread modMailThread);
}
