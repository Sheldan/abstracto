package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExperienceLevelServiceBean implements ExperienceLevelService {

    @Autowired
    private ExperienceLevelManagementService experienceLevelManagementService;

    private void createExperienceLevel(Integer level, Long experienceNeeded) {
        if(!experienceLevelManagementService.levelExists(level)) {
            log.trace("Creating new experience level {} with experience needed {}.", level, experienceNeeded);
            experienceLevelManagementService.createExperienceLevel(level, experienceNeeded);
        }
    }

    /**
     * Creates all {@link AExperienceLevel} until (including 0) up until the passed level
     * @param level The max level to create {@link dev.sheldan.abstracto.experience.models.database.AExperienceLevel} for
     */
    @Override
    public void createLevelsUntil(Integer level) {
        createExperienceLevel(0, 0L);
        long experience = 0L;
        for (int i = 1; i < level; i++) {
            experience = experience + calculateExperienceForLevel(i - 1);
            createExperienceLevel(i, experience);
        }
    }

    /**
     * Calculates the required experience to reach this level. This calculated experience is relative, in the sense
     * the returned experience is the increment from the experience requirement from the level before.
     * @param level The level to calculate the experience amount for
     * @return The needed experience to reach this level, if the user already has the level below the passed one
     */
    private Long calculateExperienceForLevel(Integer level) {
        return 5L * (level * level) + 50 * level + 100;
    }

    @Override
    public Long calculateExperienceToNextLevel(Integer level, Long currentExperience) {
        AExperienceLevel nextLevel = experienceLevelManagementService.getLevel(level + 1).orElseThrow(() -> new AbstractoRunTimeException(String.format("Could not find level %s", level)));
        return nextLevel.getExperienceNeeded() - currentExperience;
    }

}
