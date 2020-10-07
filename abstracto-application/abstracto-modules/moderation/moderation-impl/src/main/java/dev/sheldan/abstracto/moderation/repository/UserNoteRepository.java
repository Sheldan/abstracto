package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.UserNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;

@Repository
public interface UserNoteRepository extends JpaRepository<UserNote, Long> {
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<UserNote> findByUser(AUserInAServer aUserInAServer);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    List<UserNote> findByUser_ServerReference(AServer server);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByUserNoteId_IdAndUserNoteId_ServerId(@NonNull Long aLong, Long serverId);

    void deleteByUserNoteId_IdAndUserNoteId_ServerId(@NonNull Long aLong, Long serverId);

}
