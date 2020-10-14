package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.models.database.ADefaultConfig;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.test.MockUtils;
import dev.sheldan.abstracto.utility.service.StarboardServiceBean;
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
public class StarboardConfigListenerTest {

    @InjectMocks
    private StarboardConfigListener testUnit;

    @Mock
    private ConfigManagementService configManagementService;

    @Mock
    private DefaultConfigManagementService defaultConfigManagementService;

    @Captor
    private ArgumentCaptor<String> configKeyCaptor;

    @Test
    public void testUpdateServerConfig() {
        AServer server = MockUtils.getServer();
        int numberOfLevels = 4;
        ADefaultConfig config = ADefaultConfig.builder().longValue((long)numberOfLevels).build();
        when(defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LEVELS_CONFIG_KEY)).thenReturn(config);
        when(defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LVL_CONFIG_PREFIX + 1)).thenReturn(config);
        when(defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LVL_CONFIG_PREFIX + 2)).thenReturn(config);
        when(defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LVL_CONFIG_PREFIX + 3)).thenReturn(config);
        when(defaultConfigManagementService.getDefaultConfig(StarboardServiceBean.STAR_LVL_CONFIG_PREFIX + 4)).thenReturn(config);
        testUnit.updateServerConfig(server);
        verify(configManagementService, times(numberOfLevels)).createIfNotExists(eq(server.getId()), configKeyCaptor.capture(), anyLong());
        List<String> keys = configKeyCaptor.getAllValues();
        for (int i = 0; i < numberOfLevels; i++) {
            Assert.assertEquals(StarboardServiceBean.STAR_LVL_CONFIG_PREFIX + (i + 1), keys.get(i));
        }
        Assert.assertEquals(numberOfLevels, keys.size());
    }

}
