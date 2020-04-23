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
     * @param level The level the {@link ARole} should be awareded at
     * @param server The {@link AServer} for which this configuration should be done
     */
    void setRoleToLevel(ARole role, Integer level, AServer server, AChannel channel);

    /**
     * Removes the role from the {@link dev.sheldan.abstracto.experience.models.database.AExperienceRole} configuration
     * @param role The {@link ARole} to remove from the {@link dev.sheldan.abstracto.experience.models.database.AExperienceRole}
     *             configuration
     * @param server The {@link AServer} for which the {@link ARole} should be removed from the configuration
     */
    void unsetRole(ARole role, AServer server, AChannel feedbackChannel);

    /**
     * Calculates the appropriate {@link AExperienceRole} based on the provided list of {@link AExperienceRole}
     * @param userExperience The {@link AUserExperience} containing the level to calculate the {@link AExperienceRole}
     * @param roles The role configuration to be used when calculating the appropriate {@link AExperienceRole}
     * @return The best matching {@link AExperienceRole} accordign to the experience in the provided {@link AUserExperience}
     */
    AExperienceRole calculateRole(AUserExperience userExperience, List<AExperienceRole> roles);

    AExperienceLevel getLevelOfNextRole(AExperienceLevel startLevel, AServer server);
}
