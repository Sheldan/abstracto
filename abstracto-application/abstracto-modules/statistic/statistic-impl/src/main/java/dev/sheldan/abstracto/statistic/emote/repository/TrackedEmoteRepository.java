package dev.sheldan.abstracto.statistic.emote.repository;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository responsible for database operations on {@link TrackedEmote}
 */
@Repository
public interface TrackedEmoteRepository extends JpaRepository<TrackedEmote, ServerSpecificId> {
    /**
     * Retrieves all {@link TrackedEmote} from a {@link dev.sheldan.abstracto.core.models.database.AServer} via the ID
     * which are not deleted and not external
     * @param serverId The ID of the {@link dev.sheldan.abstracto.core.models.database.AServer} to retrieve the {@link TrackedEmote} for
     * @return A list of {@link TrackedEmote} from the {@link dev.sheldan.abstracto.core.models.database.AServer} identified by ID, which are not deleted or external
     */
    List<TrackedEmote> findByTrackedEmoteId_ServerIdAndDeletedFalseAndExternalFalse(Long serverId);

    /**
     * Retrieves all {@link TrackedEmote} from a {@link dev.sheldan.abstracto.core.models.database.AServer} via the ID
     * which have their tracking enabled
     * @param serverId The ID of the {@link dev.sheldan.abstracto.core.models.database.AServer} to retrieve the {@link TrackedEmote} for
     * @return A list of {@link TrackedEmote} from the {@link dev.sheldan.abstracto.core.models.database.AServer} identified by ID, which have their tracking enabled
     */
    List<TrackedEmote> findByTrackedEmoteId_ServerIdAndTrackingEnabledTrue(Long serverId);


    /**
     * Retrieves all {@link TrackedEmote} from a {@link dev.sheldan.abstracto.core.models.database.AServer} via the ID
     * @param serverId The ID of the {@link dev.sheldan.abstracto.core.models.database.AServer} to retrieve the {@link TrackedEmote} for
     * @return A list of {@link TrackedEmote} from the {@link dev.sheldan.abstracto.core.models.database.AServer} identified by ID
     */
    List<TrackedEmote> findByTrackedEmoteId_ServerId(Long serverId);
}
