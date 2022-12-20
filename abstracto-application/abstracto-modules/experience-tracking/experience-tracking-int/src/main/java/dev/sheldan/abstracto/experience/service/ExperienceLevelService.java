package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;

/**
 * Service responsible for operations on {@link dev.sheldan.abstracto.experience.model.database.AExperienceLevel experienceLevel}
 * This includes creating and calculations.
 */
public interface ExperienceLevelService {

    /**
     * Creates all the levels up until the given level.
     * @param level The max level to create {@link dev.sheldan.abstracto.experience.model.database.AExperienceLevel level} for
     */
    void createLevelsUntil(Integer level);

    /**
     * Calculates the required experience until the next level is reached according to the provided experience and
     * provided level.
     * @param level The level to base the calculation of
     * @param currentExperience The current total experience
     * @return The amount of experience required necessary to reach the next level
     */
    Long calculateExperienceToNextLevel(Integer level, Long currentExperience);
    AExperienceLevel calculateNextLevel(Integer level);

    /**
     * Calculates the required experience to reach this level. This calculated experience is relative, in the sense that
     * the returned experience is the difference between the level before the provided one
     * @param level The level to calculate the experience amount for
     * @return The needed experience to reach this level, if the user already has the level below the provided one
     */
    Long calculateExperienceForLevel(Integer level);
}
