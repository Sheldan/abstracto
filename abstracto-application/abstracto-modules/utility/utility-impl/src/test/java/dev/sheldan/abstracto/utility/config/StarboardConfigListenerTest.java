package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ConfigManagementService;
import dev.sheldan.abstracto.test.MockUtils;
import dev.sheldan.abstracto.utility.service.StarboardServiceBean;
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
public class StarboardConfigListenerTest {

    @InjectMocks
    private StarboardConfigListener testUnit;

    @Mock
    private StarboardConfig starboardConfig;

    @Mock
    private ConfigManagementService configManagementService;

    @Captor
    private ArgumentCaptor<String> configKeyCaptor;

    @Test
    public void testUpdateServerConfig() {
        AServer server = MockUtils.getServer();
        List<Integer> levels = Arrays.asList(1, 2, 3);
        when(starboardConfig.getLvl()).thenReturn(levels);
        testUnit.updateServerConfig(server);
        verify(configManagementService, times(levels.size())).createIfNotExists(eq(server.getId()), configKeyCaptor.capture(), anyLong());
        List<String> keys = configKeyCaptor.getAllValues();
        for (int i = 0; i < levels.size(); i++) {
            Assert.assertEquals(StarboardServiceBean.STAR_LVL_CONFIG_PREFIX + (i + 1), keys.get(i));
        }
        Assert.assertEquals(levels.size(), keys.size());
    }

}
