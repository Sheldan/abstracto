package dev.sheldan.abstracto.moderation.models.template.commands;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PurgeStatusUpdateModel {
    private Integer currentlyDeleted;
    private Integer totalToDelete;
}
