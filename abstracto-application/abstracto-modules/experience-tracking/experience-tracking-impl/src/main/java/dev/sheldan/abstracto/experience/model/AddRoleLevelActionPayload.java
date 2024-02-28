package dev.sheldan.abstracto.experience.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddRoleLevelActionPayload implements LevelActionPayload {
    private Long roleId;
}
