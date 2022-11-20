package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.template.LevelRole;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service providing several methods surrounding {@link dev.sheldan.abstracto.experience.model.database.AExperienceRole experienceRole}.
 */
public interface ExperienceRoleService {

    CompletableFuture<Void> setRoleToLevel(Role role, Integer level, GuildMessageChannel messageChannel);

    CompletableFuture<Void> unsetRoles(ARole role, GuildMessageChannel messageChannel);
    List<AExperienceRole> getExperienceRolesAtLevel(Integer level, AServer server);
    CompletableFuture<Void> unsetRoles(List<ARole> roles, GuildMessageChannel messageChannel);

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
