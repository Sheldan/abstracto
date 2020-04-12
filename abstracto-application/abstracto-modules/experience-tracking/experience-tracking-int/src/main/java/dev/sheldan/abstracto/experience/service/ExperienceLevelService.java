package dev.sheldan.abstracto.experience.service;

public interface ExperienceLevelService {
    void createExperienceLevel(Integer level, Long experienceNeeded);
    Long calculateExperienceForLevel(Integer level);
    Long calculateExperienceToNextLevel(Integer level, Long currentExperience);
}
