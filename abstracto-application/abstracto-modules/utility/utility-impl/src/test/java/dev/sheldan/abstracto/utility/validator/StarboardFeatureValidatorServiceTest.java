package dev.sheldan.abstracto.utility.validator;

import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureValidatorService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.test.MockUtils;
import dev.sheldan.abstracto.utility.service.StarboardServiceBean;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarboardFeatureValidatorServiceTest {

    @InjectMocks
    private StarboardFeatureValidatorService testUnit;

    @Mock
    private FeatureValidatorService featureValidatorService;

    @Mock
    private DefaultConfigManagementService defaultConfigManagementService;

    @Captor
    private ArgumentCaptor<String> configKeyCaptor;

    @Test
    public void testStarboardFeatureConfig() {
        AServer server = MockUtils.getServer();
        int levelCount = 4;
        SystemConfigProperty config = Mockito.mock(SystemConfigProperty.class);
        when(config.getLongValue()).thenReturn((long) levelCount);
        when(defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LEVELS_CONFIG_KEY)).thenReturn(config);

        testUnit.featureIsSetup(null, server, null);
        verify(featureValidatorService, times(levelCount)).checkSystemConfig(configKeyCaptor.capture(), eq(server), any());
        List<String> allValues = configKeyCaptor.getAllValues();
        for (int i = 0; i < allValues.size(); i++) {
            String key = allValues.get(i);
            Assert.assertEquals("starLvl" + ( i + 1 ), key);
        }
        Assert.assertEquals(levelCount, allValues.size());
    }

}
