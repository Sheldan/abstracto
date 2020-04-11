package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;

import java.util.List;

public interface ExperienceLevelManagementService {
    AExperienceLevel createExperienceLevel(Integer level, Long neededExperience);
    boolean levelExists(Integer level);
    AExperienceLevel getLevel(Integer level);
    AExperienceLevel getLevelClosestTo(Long experience);
    List<AExperienceLevel> getLevelConfig();
}
