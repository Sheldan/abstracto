package dev.sheldan.abstracto.moderation.model.template.command;

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
