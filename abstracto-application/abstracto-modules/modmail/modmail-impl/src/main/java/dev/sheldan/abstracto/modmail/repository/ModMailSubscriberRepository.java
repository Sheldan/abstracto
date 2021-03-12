package dev.sheldan.abstracto.modmail.repository;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to manage the stored {@link ModMailThreadSubscriber} instances
 */
@Repository
public interface ModMailSubscriberRepository extends JpaRepository<ModMailThreadSubscriber, Long> {
    List<ModMailThreadSubscriber> findByThreadReference(ModMailThread thread);

    boolean existsBySubscriberAndThreadReference(AUserInAServer aUserInAServer, ModMailThread modMailThread);

    void deleteBySubscriberAndThreadReference(AUserInAServer aUserInAServer, ModMailThread modMailThread);
}
