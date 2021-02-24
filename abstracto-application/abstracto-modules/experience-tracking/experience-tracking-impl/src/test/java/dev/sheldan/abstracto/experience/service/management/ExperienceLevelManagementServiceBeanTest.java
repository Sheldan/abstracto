package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.experience.models.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.repository.ExperienceLevelRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExperienceLevelManagementServiceBeanTest {

    @InjectMocks
    private ExperienceLevelManagementServiceBean testUnit;

    @Mock
    private ExperienceLevelRepository experienceLevelRepository;

    @Test
    public void testCreateExperienceLevel() {
        Integer level = 1;
        Long neededExperience = 100L;
        AExperienceLevel createdLevel = getLevel(level, neededExperience);
        when(experienceLevelRepository.save(any(AExperienceLevel.class))).thenReturn(createdLevel);
        AExperienceLevel experienceLevel = testUnit.createExperienceLevel(level, neededExperience);
        Assert.assertEquals(experienceLevel.getLevel(), createdLevel.getLevel());
        Assert.assertEquals(experienceLevel.getExperienceNeeded(), createdLevel.getExperienceNeeded());
    }

    @Test
    public void testLevelExists() {
        int levelValue = 1;
        when(experienceLevelRepository.existsById(levelValue)).thenReturn(true);
        Assert.assertTrue(testUnit.levelExists(levelValue));
    }

    @Test
    public void testFindLevel() {
        int levelValue = 1;
        long experienceAmount = 100L;
        Optional<AExperienceLevel> level = Optional.of(getLevel(levelValue, experienceAmount));
        when(experienceLevelRepository.findById(levelValue)).thenReturn(level);
        Optional<AExperienceLevel> foundLevelOptional = testUnit.getLevel(levelValue);
        Assert.assertTrue(foundLevelOptional.isPresent());
        if(foundLevelOptional.isPresent()) {
            AExperienceLevel foundLevel = foundLevelOptional.get();
            Assert.assertEquals(experienceAmount, foundLevel.getExperienceNeeded().longValue());
            Assert.assertEquals(levelValue, foundLevel.getLevel().intValue());
        } else {
            Assert.fail();
        }
    }

    @Test
    public void testFindLevelAndNoLevelFound() {
        int levelValue = 1;
        Optional<AExperienceLevel> level = Optional.empty();
        when(experienceLevelRepository.findById(levelValue)).thenReturn(level);
        Optional<AExperienceLevel> foundLevelOptional = testUnit.getLevel(levelValue);
        Assert.assertFalse(foundLevelOptional.isPresent());
    }

    @Test
    public void findAllLevelConfigurations() {
        AExperienceLevel availableLevel = getLevel(1, 100L);
        AExperienceLevel availableLevel2 = getLevel(2, 200L);
        List<AExperienceLevel> levelConfig = Arrays.asList(availableLevel, availableLevel2);
        when(experienceLevelRepository.findAll()).thenReturn(levelConfig);
        List<AExperienceLevel> foundLevelConfig = testUnit.getLevelConfig();
        AExperienceLevel firstLevelConfig = foundLevelConfig.get(0);
        Assert.assertEquals(100L, firstLevelConfig.getExperienceNeeded().longValue());
        Assert.assertEquals(1, firstLevelConfig.getLevel().intValue());
        AExperienceLevel secondLevelConfig = foundLevelConfig.get(1);
        Assert.assertEquals(200L, secondLevelConfig.getExperienceNeeded().longValue());
        Assert.assertEquals(2, secondLevelConfig.getLevel().intValue());
    }

    private AExperienceLevel getLevel(Integer level, Long neededExperience) {
        return AExperienceLevel
                .builder()
                .level(level)
                .experienceNeeded(neededExperience)
                .build();
    }
}
