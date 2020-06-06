package dev.sheldan.abstracto.utility.config;

import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StarboardDefaultConfigListenerTest {

    @InjectMocks
    private StarboardDefaultConfigListener testUnit;

    @Mock
    private DefaultConfigManagementService defaultConfigManagementService;

    @Mock
    private StarboardConfig starboardConfig;

    @Captor
    private ArgumentCaptor<String> configKeyCaptor;

    @Test
    public void testDefaultConfig() {
        List<Integer> levels = Arrays.asList(1, 2, 3);
        when(starboardConfig.getLvl()).thenReturn(levels);
        testUnit.handleContextRefreshEvent(null);
        verify(defaultConfigManagementService, times(levels.size())).createDefaultConfig(configKeyCaptor.capture(), anyLong());
        List<String> keys = configKeyCaptor.getAllValues();
        for (int i = 0; i < levels.size(); i++) {
            Assert.assertEquals(StarboardServiceBean.STAR_LVL_CONFIG_PREFIX + (i + 1), keys.get(i));
        }
        Assert.assertEquals(levels.size(), keys.size());
    }
}
