package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.repository.ExperienceLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ExperienceLevelManagementServiceBean implements ExperienceLevelManagementService {

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
    public Optional<AExperienceLevel> getLevelOptional(Integer level) {
        return experienceLevelRepository.findById(level);
    }

    @Override
    public AExperienceLevel getLevel(Integer level) {
        return getLevelOptional(level).orElseThrow(() -> new AbstractoRunTimeException("Level not found."));
    }

    @Override
    public List<AExperienceLevel> getLevelConfig() {
        return experienceLevelRepository.findAll();
    }

    @Override
    public Map<Integer, AExperienceLevel> getLevelConfigAsMap() {
        return getLevelConfig().stream().collect(Collectors.toMap(AExperienceLevel::getLevel, Function.identity()));
    }
}
