package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExperienceLevelServiceBean implements ExperienceLevelService {

    @Autowired
    private ExperienceLevelManagementService experienceLevelManagementService;

    /**
     * Creates experience level if it does not yet exist.
     * @param level The level to create
     * @param experienceNeeded The total amount of experience needed to reach the given level
     */
    private void createExperienceLevel(Integer level, Long experienceNeeded) {
        if(!experienceLevelManagementService.levelExists(level)) {
            log.info("Creating new experience level {} with experience needed {}.", level, experienceNeeded);
            experienceLevelManagementService.createExperienceLevel(level, experienceNeeded);
        }
    }

    /**
     * Creates all {@link AExperienceLevel} until (including 0) up until the passed level
     * @param level The max level to create {@link dev.sheldan.abstracto.experience.model.database.AExperienceLevel} for
     */
    @Override
    public void createLevelsUntil(Integer level) {
        log.info("Creating experience levels until level {}.", level);
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
    @Override
    public Long calculateExperienceForLevel(Integer level) {
        if(level < 0) {
            throw new IllegalArgumentException("Level should not be less to 0.");
        }
        return 5L * (level * level) + 50 * level + 100;
    }

    @Override
    public Long calculateExperienceToNextLevel(Integer level, Long currentExperience) {
        AExperienceLevel nextLevel = calculateNextLevel(level);
        return nextLevel.getExperienceNeeded() - currentExperience;
    }

    @Override
    public AExperienceLevel calculateNextLevel(Integer level) {
        return experienceLevelManagementService.getLevelOptional(level + 1)
                .orElseThrow(() -> new AbstractoRunTimeException(String.format("Could not find level %s", level)));
    }

}
