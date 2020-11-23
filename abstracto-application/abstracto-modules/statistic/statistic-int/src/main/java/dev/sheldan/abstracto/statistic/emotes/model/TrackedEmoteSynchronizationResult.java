package dev.sheldan.abstracto.statistic.emotes.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Model containing the result of `syncTrackedEmotes`. The two numbers are the amount of {@link dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote}
 * which were created and marked as deleted.
 *
 */
@Getter
@Setter
@Builder
public class TrackedEmoteSynchronizationResult {
    /**
     * The amount of {@link dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote} which were created because of the synchronization
     */
    private Long emotesAdded;
    /**
     * The amount of {@link dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote} which were marked as deleted because of the synchronization
     */
    private Long emotesMarkedDeleted;
}
