package dev.sheldan.abstracto.statistic.emotes.service;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emotes.converter.EmoteStatsConverter;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.management.UsedEmoteManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UsedEmoteServiceBeanTest {

    @InjectMocks
    private UsedEmoteServiceBean testUnit;

    @Mock
    private EmoteStatsConverter converter;

    @Mock
    private UsedEmoteManagementService usedEmoteManagementService;

    @Mock
    private AServer server;

    @Mock
    private EmoteStatsModel emoteStatsModel;

    @Mock
    private TrackedEmote trackedEmote;

    private static final Instant pointInTime = Instant.parse("2020-12-12T00:00:00.00Z");
    private static final Long SERVER_ID = 4L;
    private static final Long EMOTE_ID = 7L;

    @Test
    public void testGetEmoteStatsForServerSince() {
        List<EmoteStatsResult> mockedEmoteStatsResult = getMockedStatsResult();
        when(usedEmoteManagementService.loadAllEmoteStatsForServerSince(eq(server), any(Instant.class))).thenReturn(mockedEmoteStatsResult);
        when(converter.fromEmoteStatsResults(mockedEmoteStatsResult)).thenReturn(emoteStatsModel);
        EmoteStatsModel result = testUnit.getEmoteStatsForServerSince(server, pointInTime);
        Assert.assertEquals(emoteStatsModel, result);
    }

    @Test
    public void testGetDeletedEmoteStatsForServerSince() {
        List<EmoteStatsResult> mockedEmoteStatsResult = getMockedStatsResult();
        when(usedEmoteManagementService.loadDeletedEmoteStatsForServerSince(eq(server), any(Instant.class))).thenReturn(mockedEmoteStatsResult);
        when(converter.fromEmoteStatsResults(mockedEmoteStatsResult)).thenReturn(emoteStatsModel);
        EmoteStatsModel result = testUnit.getDeletedEmoteStatsForServerSince(server, pointInTime);
        Assert.assertEquals(emoteStatsModel, result);
    }

    @Test
    public void testGetExternalEmoteStatsForServerSince() {
        List<EmoteStatsResult> mockedEmoteStatsResult = getMockedStatsResult();
        when(usedEmoteManagementService.loadExternalEmoteStatsForServerSince(eq(server), any(Instant.class))).thenReturn(mockedEmoteStatsResult);
        when(converter.fromEmoteStatsResults(mockedEmoteStatsResult)).thenReturn(emoteStatsModel);
        EmoteStatsModel result = testUnit.getExternalEmoteStatsForServerSince(server, pointInTime);
        Assert.assertEquals(emoteStatsModel, result);
    }

    @Test
    public void testGetActiveEmoteStatsForServerSince() {
        List<EmoteStatsResult> mockedEmoteStatsResult = getMockedStatsResult();
        when(usedEmoteManagementService.loadActiveEmoteStatsForServerSince(eq(server), any(Instant.class))).thenReturn(mockedEmoteStatsResult);
        when(converter.fromEmoteStatsResults(mockedEmoteStatsResult)).thenReturn(emoteStatsModel);
        EmoteStatsModel result = testUnit.getActiveEmoteStatsForServerSince(server, pointInTime);
        Assert.assertEquals(emoteStatsModel, result);
    }

    @Test
    public void testPurgeEmoteUsagesSince() {
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID));
        testUnit.purgeEmoteUsagesSince(trackedEmote, pointInTime);
        verify(usedEmoteManagementService, times(1)).purgeEmoteUsagesSince(trackedEmote, pointInTime);
    }

    @Test
    public void testPurgeEmoteUsages() {
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID));
        testUnit.purgeEmoteUsages(trackedEmote);
        verify(usedEmoteManagementService, times(1)).purgeEmoteUsagesSince(trackedEmote, Instant.EPOCH);
    }

    private List<EmoteStatsResult> getMockedStatsResult() {
        return Arrays.asList(Mockito.mock(EmoteStatsResult.class), Mockito.mock(EmoteStatsResult.class));
    }

}
