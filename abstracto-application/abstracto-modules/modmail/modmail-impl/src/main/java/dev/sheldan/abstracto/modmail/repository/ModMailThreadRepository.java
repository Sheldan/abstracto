package dev.sheldan.abstracto.modmail.repository;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.models.database.ModMailThreadState;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * Repository to manage the stored {@link ModMailThread} instances
 */
@Repository
public interface ModMailThreadRepository extends JpaRepository<ModMailThread, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    ModMailThread findByChannel(AChannel channel);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<ModMailThread> findByUser(AUserInAServer aUserInAServer);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    ModMailThread findTopByUserOrderByClosedDesc(AUserInAServer aUserInAServer);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<ModMailThread> findByUser_UserReferenceAndStateNot(AUser user, ModMailThreadState state);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByUser_UserReferenceAndStateNot(AUser user, ModMailThreadState state);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<ModMailThread> findByServerAndState(AServer server, ModMailThreadState state);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    ModMailThread findByUserAndStateNot(AUserInAServer userInAServer, ModMailThreadState state);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByUserAndStateNot(AUserInAServer userInAServer, ModMailThreadState state);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<ModMailThread> findByUserAndState(AUserInAServer userInAServer, ModMailThreadState state);

    @NotNull
    @Override
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    Optional<ModMailThread> findById(@NonNull Long aLong);
}
