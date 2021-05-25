package dev.sheldan.abstracto.experience.service.management;

import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.repository.ExperienceLevelRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
        AExperienceLevel createdLevel = Mockito.mock(AExperienceLevel.class);
        when(createdLevel.getLevel()).thenReturn(level);
        when(createdLevel.getExperienceNeeded()).thenReturn(neededExperience);
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
        AExperienceLevel level = Mockito.mock(AExperienceLevel.class);
        when(level.getLevel()).thenReturn(levelValue);
        when(level.getExperienceNeeded()).thenReturn(experienceAmount);
        when(experienceLevelRepository.findById(levelValue)).thenReturn(Optional.of(level));
        Optional<AExperienceLevel> foundLevelOptional = testUnit.getLevelOptional(levelValue);
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
        Optional<AExperienceLevel> foundLevelOptional = testUnit.getLevelOptional(levelValue);
        Assert.assertFalse(foundLevelOptional.isPresent());
    }

    @Test
    public void findAllLevelConfigurations() {
        AExperienceLevel availableLevel = Mockito.mock(AExperienceLevel.class);
        when(availableLevel.getLevel()).thenReturn(1);
        when(availableLevel.getExperienceNeeded()).thenReturn(100L);
        AExperienceLevel availableLevel2 = Mockito.mock(AExperienceLevel.class);
        when(availableLevel2.getLevel()).thenReturn(2);
        when(availableLevel2.getExperienceNeeded()).thenReturn(200L);
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

}
