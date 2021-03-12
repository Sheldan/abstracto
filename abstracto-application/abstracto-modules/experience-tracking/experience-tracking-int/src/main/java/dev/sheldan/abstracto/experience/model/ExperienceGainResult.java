package dev.sheldan.abstracto.experience.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CompletableFuture;

/**
 * This object is used to determine the result of calculating the gained experience.
 * It is used in the schedule job responsible to calculate the changes after experience has been awarded.
 * This changes can include a level or experience role change.
 */
@Getter
@Setter
@Builder
public class ExperienceGainResult {
    /**
     * The calculation result contained in a {@link CompletableFuture future}. The future is necessary, because the calculation both calculates the new role
     * and removes/adds {@link net.dv8tion.jda.api.entities.Role role} to the {@link net.dv8tion.jda.api.entities.Member member}
     */
    private CompletableFuture<RoleCalculationResult> calculationResult;
    /**
     * The ID of the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer user} for which this is the result
     */
    private Long userInServerId;
    /**
     * The amount of experience the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer user} has
     */
    private Long newExperience;
    /**
     * The ID of the {@link dev.sheldan.abstracto.core.models.database.AServer server} this calculation took place in
     */
    private Long serverId;
    /**
     * The new level the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer user} reached. Might be the same as the old.
     */
    private Integer newLevel;
    /**
     * The new amount of messages of the {@link dev.sheldan.abstracto.core.models.database.AUserInAServer user} in this server which were counted bý experience tracking
     */
    private Long newMessageCount;
    /**
     * Whether or not a {@link dev.sheldan.abstracto.experience.model.database.AUserExperience experience} object needs to be created after the calculation.
     * This happens if the object did not exist yet, but the calculation was done regardless.
     */
    @Builder.Default
    private boolean createUserExperience = false;
}
