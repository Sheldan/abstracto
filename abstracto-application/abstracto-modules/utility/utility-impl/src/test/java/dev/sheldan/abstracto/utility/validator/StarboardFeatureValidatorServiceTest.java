package dev.sheldan.abstracto.utility.validator;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureValidatorService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.config.StarboardConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarboardFeatureValidatorServiceTest {

    @InjectMocks
    private StarboardFeatureValidatorService testUnit;

    @Mock
    private StarboardConfig starboardConfig;

    @Mock
    private FeatureValidatorService featureValidatorService;

    @Captor
    private ArgumentCaptor<String> configKeyCaptor;

    @Test
    public void testStarboardFeatureConfig() {
        List<Integer> definedLevels = Arrays.asList(1, 2, 3);
        when(starboardConfig.getLvl()).thenReturn(definedLevels);
        AServer server = MockUtils.getServer();
        testUnit.featureIsSetup(null, server, null);
        verify(featureValidatorService, times(definedLevels.size())).checkSystemConfig(configKeyCaptor.capture(), eq(server), any());
        List<String> allValues = configKeyCaptor.getAllValues();
        for (int i = 0; i < allValues.size(); i++) {
            String key = allValues.get(i);
            Assert.assertEquals("starLvl"+ ( i + 1 ), key);
        }
        Assert.assertEquals(definedLevels.size(), allValues.size());
    }

}
