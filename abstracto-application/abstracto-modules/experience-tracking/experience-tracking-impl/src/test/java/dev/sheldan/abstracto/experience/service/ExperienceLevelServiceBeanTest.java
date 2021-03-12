package dev.sheldan.abstracto.experience.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.experience.model.database.AExperienceLevel;
import dev.sheldan.abstracto.experience.service.management.ExperienceLevelManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExperienceLevelServiceBeanTest {

    @InjectMocks
    private ExperienceLevelServiceBean testingUnit;

    @Mock
    private ExperienceLevelManagementService experienceLevelManagementService;

    @Test
    public void createAFewLevels() {
        Integer levelCount = 10;
        Integer existingLevelCount = levelCount - 5;
        for (int i = 0; i < levelCount; i++) {
            when(experienceLevelManagementService.levelExists(i)).thenReturn(i < existingLevelCount);
        }
        testingUnit.createLevelsUntil(levelCount);
        verify(experienceLevelManagementService, times(levelCount - existingLevelCount)).createExperienceLevel(anyInt(), anyLong());
    }

    @Test
    public void testExperienceToNextLevelCalculation() {
        AExperienceLevel level = mock(AExperienceLevel.class);
        when(level.getExperienceNeeded()).thenReturn(15L);
        when(experienceLevelManagementService.getLevel(51)).thenReturn(Optional.of(level));
        Long experience = testingUnit.calculateExperienceToNextLevel(50, 10L);
        Assert.assertEquals(5L, experience.longValue());
    }

    @Test
    public void testRequiredExperienceCalculation() {
        Assert.assertEquals(15100L, testingUnit.calculateExperienceForLevel(50).longValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequiredExperienceCalculationIllegalLevel() {
        testingUnit.calculateExperienceForLevel(-1);
    }

    @Test(expected = AbstractoRunTimeException.class)
    public void testExperienceToNextLevelCalculationOverExistingLevel() {
        when(experienceLevelManagementService.getLevel(51)).thenReturn(Optional.empty());
        testingUnit.calculateExperienceToNextLevel(50, 10L);
    }

}
