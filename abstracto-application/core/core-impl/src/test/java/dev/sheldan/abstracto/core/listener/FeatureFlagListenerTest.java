package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.DefaultFeatureFlag;
import dev.sheldan.abstracto.core.service.management.DefaultFeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FeatureFlagListenerTest {
    
    @InjectMocks
    private FeatureFlagListener testUnit;

    @Mock
    private FeatureFlagManagementService service;

    @Mock
    private DefaultFeatureFlagManagementService defaultFeatureFlagManagementService;

    @Mock
    private DefaultFeatureFlag defaultFeatureFlag1;

    @Mock
    private DefaultFeatureFlag defaultFeatureFlag2;

    @Mock
    private AFeature aFeature1;

    @Mock
    private AFeature aFeature2;

    @Mock
    private AServer server;

    @Mock
    private AFeatureFlag aFeatureFlag;

    private static final Long SERVER_ID = 8L;

    @Test
    public void testCreateMultipleOneAlreadyExisting() {
        when(server.getId()).thenReturn(SERVER_ID);
        when(defaultFeatureFlagManagementService.getAllDefaultFeatureFlags()).thenReturn(Arrays.asList(defaultFeatureFlag1, defaultFeatureFlag2));
        when(defaultFeatureFlag1.getFeature()).thenReturn(aFeature1);
        when(defaultFeatureFlag2.getFeature()).thenReturn(aFeature2);
        when(defaultFeatureFlag2.isEnabled()).thenReturn(true);
        when(service.featureFlagExists(aFeature1, server)).thenReturn(true);
        when(service.featureFlagExists(aFeature2, server)).thenReturn(false);
        when(service.createFeatureFlag(aFeature2, SERVER_ID, true)).thenReturn(aFeatureFlag);
        testUnit.updateServerConfig(server);
    }
}