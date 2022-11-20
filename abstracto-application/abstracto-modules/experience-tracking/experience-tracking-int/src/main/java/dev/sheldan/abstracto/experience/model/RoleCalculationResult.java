package dev.sheldan.abstracto.experience.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoleCalculationResult {
    private Long oldRoleId;
    private Long newRoleId;
}
