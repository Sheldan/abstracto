package dev.sheldan.abstracto.experience.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RemoveRoleLevelActionPayload implements LevelActionPayload {
    private Long roleId;
}
