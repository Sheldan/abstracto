package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.UserNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNoteRepository extends JpaRepository<UserNote, Long> {
    List<UserNote> findByUser(AUserInAServer aUserInAServer);

    List<UserNote> findByUser_ServerReference(AServer server);

    boolean existsByUserNoteId_IdAndUserNoteId_ServerId(@NonNull Long userNoteId, Long serverId);

    void deleteByUserNoteId_IdAndUserNoteId_ServerId(@NonNull Long aLong, Long serverId);

    Optional<UserNote> findByUserNoteId_IdAndUserNoteId_ServerId(Long userNoteId, Long serverId);

}
