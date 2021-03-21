package dev.sheldan.abstracto.moderation.job;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.moderation.config.feature.WarningDecayFeatureConfig;
import dev.sheldan.abstracto.moderation.service.WarnService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class WarnDecayJobTest {

    @InjectMocks
    private WarnDecayJob testUnit;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private FeatureFlagService featureFlagService;

    @Mock
    private WarningDecayFeatureConfig warningDecayFeatureConfig;

    @Mock
    private WarnService warnService;

    @Mock
    private AServer firstServer;

    @Mock
    private AServer secondServer;

    private static final Long SERVER_ID = 1L;
    private static final Long SERVER_ID_2 = 2L;

    @Test
    public void executeJobForNoServers() throws JobExecutionException {
        when(serverManagementService.getAllServers()).thenReturn(Collections.emptyList());
        testUnit.executeInternal(null);
        verify(featureFlagService, times(0)).isFeatureEnabled(eq(warningDecayFeatureConfig), any(AServer.class));
        verify(warnService, times(0)).decayWarningsForServer(any(AServer.class));
    }

    @Test
    public void executeJobForAEnabledServer() throws JobExecutionException {
        when(firstServer.getId()).thenReturn(SERVER_ID);
        when(serverManagementService.getAllServers()).thenReturn(Arrays.asList(firstServer));
        when(featureFlagService.isFeatureEnabled(warningDecayFeatureConfig, firstServer)).thenReturn(true);
        testUnit.executeInternal(null);
        verify(warnService, times(1)).decayWarningsForServer(eq(firstServer));
    }

    @Test
    public void executeJobForADisabledServer() throws JobExecutionException {
        when(serverManagementService.getAllServers()).thenReturn(Arrays.asList(firstServer));
        when(featureFlagService.isFeatureEnabled(warningDecayFeatureConfig, firstServer)).thenReturn(false);
        testUnit.executeInternal(null);
        verify(warnService, times(0)).decayWarningsForServer(eq(firstServer));
    }

    @Test
    public void executeJobForMixedServers() throws JobExecutionException {
        when(firstServer.getId()).thenReturn(SERVER_ID);
        when(serverManagementService.getAllServers()).thenReturn(Arrays.asList(firstServer, secondServer));
        when(featureFlagService.isFeatureEnabled(warningDecayFeatureConfig, firstServer)).thenReturn(true);
        when(featureFlagService.isFeatureEnabled(warningDecayFeatureConfig, secondServer)).thenReturn(false);
        testUnit.executeInternal(null);
        verify(warnService, times(1)).decayWarningsForServer(eq(firstServer));
    }

    @Test
    public void executeJobForMultipleEnabledServers() throws JobExecutionException {
        when(firstServer.getId()).thenReturn(SERVER_ID);
        when(secondServer.getId()).thenReturn(SERVER_ID_2);
        when(serverManagementService.getAllServers()).thenReturn(Arrays.asList(firstServer, secondServer));
        when(featureFlagService.isFeatureEnabled(warningDecayFeatureConfig, firstServer)).thenReturn(true);
        when(featureFlagService.isFeatureEnabled(warningDecayFeatureConfig, secondServer)).thenReturn(true);
        testUnit.executeInternal(null);
        ArgumentCaptor<AServer> serverCaptor = ArgumentCaptor.forClass(AServer.class);
        verify(warnService, times(2)).decayWarningsForServer(serverCaptor.capture());
        List<AServer> capturedServers = serverCaptor.getAllValues();
        Assert.assertEquals(2, capturedServers.size());
        Assert.assertEquals(firstServer, capturedServers.get(0));
        Assert.assertEquals(secondServer, capturedServers.get(1));
    }

}
