package dev.sheldan.abstracto.modmail.repository;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.database.ModMailThreadSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

/**
 * Repository to manage the stored {@link ModMailThreadSubscriber} instances
 */
@Repository
public interface ModMailSubscriberRepository extends JpaRepository<ModMailThreadSubscriber, Long> {
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<ModMailThreadSubscriber> findByThreadReference(ModMailThread thread);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsBySubscriberAndThreadReference(AUserInAServer aUserInAServer, ModMailThread modMailThread);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    void deleteBySubscriberAndThreadReference(AUserInAServer aUserInAServer, ModMailThread modMailThread);
}
