package dev.sheldan.abstracto.experience.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoleCalculationResult {
    private Long experienceRoleId;
    private Long userInServerId;
}
