package dev.sheldan.abstracto.experience.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * The result of calculating the appropriate {@link dev.sheldan.abstracto.experience.model.database.AExperienceRole role} for a {@link dev.sheldan.abstracto.experience.model.database.AUserExperience user}
 * in a server.
 */
@Getter
@Setter
@Builder
public class RoleCalculationResult {
    /**
     * The ID of the {@link dev.sheldan.abstracto.experience.model.database.AExperienceRole role} which was given to the user. Can be null, in case no role is given.
     */
    private Long experienceRoleId;
    /**
     * The ID of a {@link dev.sheldan.abstracto.core.models.database.AUserInAServer user} for who the role was calculated for.
     */
    private Long userInServerId;
}
