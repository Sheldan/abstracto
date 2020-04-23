package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;

import java.util.List;

/**
 * Service responsible to create and retrieve {@link AExperienceLevel} objects in the database.
 */
public interface ExperienceLevelManagementService {
    /**
     * Creates the level referenced by the level and the needed experience in the database.
     * @param level The unique level this level should reprsent.
     * @param neededExperience The total amount of experience required to reach this level.
     * @return A newly created {@link AExperienceLevel} instance.
     */
    AExperienceLevel createExperienceLevel(Integer level, Long neededExperience);

    /**
     * Checks if a {@link AExperienceLevel} level indicated by the level exists in the database. Returns true if it does.
     * @param level The integer of the level to check for.
     * @return A boolean indicating whether or not the level exists in the database.
     */
    boolean levelExists(Integer level);

    /**
     * Retrieves a {@link AExperienceLevel} according to the given level.
     * @param level The level of the wanted {@link AExperienceLevel} to look for
     * @return If the level exists, returns the {@link AExperienceLevel}, if it does not exists, returns null.
     */
    AExperienceLevel getLevel(Integer level);

    /**
     * Loads the complete level configuration and returns all found {@link AExperienceLevel} objects from the database.
     * @return A list of {@link AExperienceLevel} objects representing the currently active configuration.
     */
    List<AExperienceLevel> getLevelConfig();
}
