package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.template.LevelRole;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service providing several methods surrounding {@link dev.sheldan.abstracto.experience.model.database.AExperienceRole experienceRole}.
 */
public interface ExperienceRoleService {
    /**
     * Creates an {@link dev.sheldan.abstracto.experience.model.database.AExperienceRole experienceRole} according to the given
     * parameters. This actually updates the {@link net.dv8tion.jda.api.entities.Member members}
     * which currently possessed the given role before and provides a display to see how far the progress is
     * @param role The {@link ARole role} to set the level to
     * @param level The new level the {@link ARole role} should be awarded at
     * @param channelId The ID of the {@link dev.sheldan.abstracto.core.models.database.AChannel} in which the status updates
     *                  should be sent to
     * @return A {@link CompletableFuture future} which completes, after all the updates on the {@link net.dv8tion.jda.api.entities.Member}
     * have been completed
     */
    CompletableFuture<Void> setRoleToLevel(Role role, Integer level, Long channelId);

    /**
     * Removes the role from the {@link dev.sheldan.abstracto.experience.model.database.AExperienceRole} configuration,
     * this will also update all the {@link net.dv8tion.jda.api.entities.Member} which previously had this role and re-calculates
     * a new {@link AExperienceRole experienceRole} for them while also updating them in the guild
     * @param role The {@link ARole} to remove from the {@link dev.sheldan.abstracto.experience.model.database.AExperienceRole}
     *             configuration
     * @param channelId The ID of the {@link dev.sheldan.abstracto.core.models.database.AChannel} in which the status updates
     *                  should be sent to
     * @return A {@link CompletableFuture future} which completes, after all the updates on the {@link net.dv8tion.jda.api.entities.Member}
     * have been completed
     */
    CompletableFuture<Void> unsetRoles(ARole role, Long channelId);
    List<AExperienceRole> getExperienceRolesAtLevel(Integer level, AServer server);
    CompletableFuture<Void> unsetRoles(List<ARole> roles, Long channelId);
    CompletableFuture<Void> unsetRoles(List<ARole> roles, Long channelId, AExperienceRole toAdd);

    /**
     * Calculates the appropriate {@link AExperienceRole experienceRole} based on the provided list of {@link AExperienceRole experienceRole}
     * @param roles The role configuration to be used when calculating the appropriate {@link AExperienceRole experienceRole}
     * @param currentLevel The level to calculate the {@link AExperienceRole experienceRole} for
     * @return The best matching {@link AExperienceRole experienceRole} according to the experience in the provided {@link AUserExperience}
     */
    AExperienceRole calculateRole(List<AExperienceRole> roles, Integer currentLevel);

    /**
     * Calculates the level at which the next role for a given level is available.
     * For example, if the given {@link AExperienceLevel} is 5, and a a {@link AExperienceRole} is awarded at 8, but none in between, this method will return
     * the {@link AExperienceLevel}.
     * @param startLevel The {@link AExperienceLevel} to start off at
     * @param server The {@link AServer} to use for the {@link AExperienceRole} configuration
     * @return The next {@link AExperienceLevel} a {@link AExperienceRole} is awarded at, this will be null if there are no roles or there is no further role to reach
     */
    AExperienceLevel getLevelOfNextRole(AExperienceLevel startLevel, AServer server);
    List<LevelRole> loadLevelRoleConfigForServer(AServer server);
}
