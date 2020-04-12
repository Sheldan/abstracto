package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExperienceLevelServiceBean implements ExperienceLevelService {

    @Autowired
    private ExperienceLevelManagementService experienceLevelManagementService;

    @Override
    public void createExperienceLevel(Integer level, Long experienceNeeded) {
        if(!experienceLevelManagementService.levelExists(level)) {
            experienceLevelManagementService.createExperienceLevel(level, experienceNeeded);
        }
    }

    @Override
    public Long calculateExperienceForLevel(Integer level) {
        return 5L * (level * level) + 50 * level + 100;
    }

    @Override
    public Long calculateExperienceToNextLevel(Integer level, Long currentExperience) {
        AExperienceLevel nextLevel = experienceLevelManagementService.getLevel(level + 1);
        return nextLevel.getExperienceNeeded() - currentExperience;
    }

}
