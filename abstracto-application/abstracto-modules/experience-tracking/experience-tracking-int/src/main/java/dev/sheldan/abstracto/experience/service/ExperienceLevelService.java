package dev.sheldan.abstracto.experience.service;

/**
 * Service responsible for operations on {@link dev.sheldan.abstracto.experience.models.database.AExperienceLevel}
 * This includes creating and calculations.
 */
public interface ExperienceLevelService {

    /**
     * Creates all the levels up until the given level.
     * @param level The max level to create {@link dev.sheldan.abstracto.experience.models.database.AExperienceLevel} for
     */
    void createLevelsUntil(Integer level);

    /**
     * Calculates the required experience until the next level is reached according to the current experience and
     * current level.
     * @param level The current level to base the calculation of
     * @param currentExperience The current experience
     * @return The amount of experience required necessary to get to one level higher as currently.
     */
    Long calculateExperienceToNextLevel(Integer level, Long currentExperience);
}
