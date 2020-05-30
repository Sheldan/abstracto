package dev.sheldan.abstracto.experience.config;

import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeatureConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExperienceDefaultConfigListenerTest {

    @InjectMocks
    private ExperienceDefaultConfigListener testUnit;

    @Mock
    private ExperienceConfig experienceConfig;

    @Mock
    private DefaultConfigManagementService service;

    @Captor
    private ArgumentCaptor<Long> configValueCaptor;

    @Captor
    private ArgumentCaptor<String> configKeyCaptor;

    @Test
    public void setSettingUp() {
        int minExp = 4;
        int maxExp = 10;
        double expMultiplier = 2;
        when(experienceConfig.getMinExp()).thenReturn(minExp);
        when(experienceConfig.getMaxExp()).thenReturn(maxExp);
        when(experienceConfig.getExpMultiplier()).thenReturn(expMultiplier);
        testUnit.handleContextRefreshEvent(null);
        verify(service, times(2)).createDefaultConfig(configKeyCaptor.capture(), configValueCaptor.capture());
        verify(service, times(1)).createDefaultConfig(ExperienceFeatureConfig.EXP_MULTIPLIER_KEY, expMultiplier);
        List<String> configKeys = configKeyCaptor.getAllValues();
        List<Long> configValues = configValueCaptor.getAllValues();
        Assert.assertEquals(ExperienceFeatureConfig.MIN_EXP_KEY, configKeys.get(0));
        Assert.assertEquals(minExp, configValues.get(0).intValue());
        Assert.assertEquals(ExperienceFeatureConfig.MAX_EXP_KEY, configKeys.get(1));
        Assert.assertEquals(maxExp, configValues.get(1).intValue());
    }
}
