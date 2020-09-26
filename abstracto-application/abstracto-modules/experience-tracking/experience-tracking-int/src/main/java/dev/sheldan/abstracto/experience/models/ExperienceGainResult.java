package dev.sheldan.abstracto.experience.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Builder
public class ExperienceGainResult {
    private CompletableFuture<RoleCalculationResult> calculationResult;
    private Long userInServerId;
    private Long newExperience;
    private Integer newLevel;
    private Long newMessageCount;
    @Builder.Default
    private boolean createUserExperience = false;
}
