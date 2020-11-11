package dev.sheldan.abstracto.statistic.emotes.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TrackedEmoteSynchronizationResult {
    private Long emotesAdded;
    private Long emotesMarkedDeleted;
}
