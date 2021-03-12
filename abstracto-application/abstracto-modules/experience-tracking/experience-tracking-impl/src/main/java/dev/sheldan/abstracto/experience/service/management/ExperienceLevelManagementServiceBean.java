package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.repository.ExperienceLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class    ExperienceLevelManagementServiceBean implements ExperienceLevelManagementService {

    @Autowired
    private ExperienceLevelRepository experienceLevelRepository;

    @Override
    public AExperienceLevel createExperienceLevel(Integer level, Long neededExperience) {
        AExperienceLevel experienceLevel = AExperienceLevel
                .builder()
                .experienceNeeded(neededExperience)
                .level(level)
                .build();
        return experienceLevelRepository.save(experienceLevel);
    }

    @Override
    public boolean levelExists(Integer level) {
        return experienceLevelRepository.existsById(level);
    }

    @Override
    public Optional<AExperienceLevel> getLevel(Integer level) {
        return experienceLevelRepository.findById(level);
    }

    @Override
    public List<AExperienceLevel> getLevelConfig() {
        return experienceLevelRepository.findAll();
    }
}
