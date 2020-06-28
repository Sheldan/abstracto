package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.models.database.AExperienceRole;
import dev.sheldan.abstracto.experience.models.database.AUserExperience;

import java.util.List;

/**
 * Service providing several methods surrounding {@link dev.sheldan.abstracto.experience.models.database.AExperienceRole}.
 */
public interface ExperienceRoleService {
    /**
     * Creates an {@link dev.sheldan.abstracto.experience.models.database.AExperienceRole} according to the given
     * parameters
     * @param role The {@link ARole} to set the level to
     * @param level The level the {@link ARole} should be awarded at
     */
    void setRoleToLevel(ARole role, Integer level, AChannel channel);

    /**
     * Removes the role from the {@link dev.sheldan.abstracto.experience.models.database.AExperienceRole} configuration
     * @param role The {@link ARole} to remove from the {@link dev.sheldan.abstracto.experience.models.database.AExperienceRole}
     *             configuration
     */
    void unsetRole(ARole role, AChannel feedbackChannel);

    /**
     * Calculates the appropriate {@link AExperienceRole} based on the provided list of {@link AExperienceRole}
     * @param userExperience The {@link AUserExperience} containing the level to calculate the {@link AExperienceRole}
     * @param roles The role configuration to be used when calculating the appropriate {@link AExperienceRole}
     * @return The best matching {@link AExperienceRole} according to the experience in the provided {@link AUserExperience}
     */
    AExperienceRole calculateRole(AUserExperience userExperience, List<AExperienceRole> roles);

    /**
     * Calculates the level at which the next role for a given level is available.
     * For example, if the given {@link AExperienceLevel} is 5, and a a {@link AExperienceRole} is awarded at 8, but none in between, this method will return
     * the {@link AExperienceLevel} 8.
     * @param startLevel The {@link AExperienceLevel} to start off at
     * @param server The {@link AServer} to use for the {@link AExperienceRole} configuration
     * @return The next {@link AExperienceLevel} a {@link AExperienceRole} is awarded at, this will be null if there are no roles or there is no further role to reach
     */
    AExperienceLevel getLevelOfNextRole(AExperienceLevel startLevel, AServer server);
}
