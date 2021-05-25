package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;

import java.util.List;
import java.util.Optional;

/**
 * Service responsible to create and retrieve {@link AExperienceLevel levels} objects in the database.
 */
public interface ExperienceLevelManagementService {
    /**
     * Creates the level referenced by the level and the needed experience in the database.
     * @param level The unique level this level should represent.
     * @param neededExperience The total amount of experience required to reach this level.
     * @return A newly created {@link AExperienceLevel level} instance.
     */
    AExperienceLevel createExperienceLevel(Integer level, Long neededExperience);

    /**
     * Checks if a {@link AExperienceLevel level} indicated by the level exists in the database. Returns true if it does.
     * @param level The integer of the level to check for.
     * @return A boolean indicating whether or not the level exists in the database.
     */
    boolean levelExists(Integer level);

    /**
     * Retrieves a {@link AExperienceLevel level} according to the given level.
     * @param level The {@link AExperienceLevel level} of the wanted level number
     * @return Returns an optional containing the {@link AExperienceLevel level} if it exists, and empty otherwise
     */
    Optional<AExperienceLevel> getLevelOptional(Integer level);
    AExperienceLevel getLevel(Integer level);

    /**
     * Loads the complete level configuration and returns all found {@link AExperienceLevel levels} from the database.
     * @return A list of {@link AExperienceLevel levels} representing the currently active configuration.
     */
    List<AExperienceLevel> getLevelConfig();
}
