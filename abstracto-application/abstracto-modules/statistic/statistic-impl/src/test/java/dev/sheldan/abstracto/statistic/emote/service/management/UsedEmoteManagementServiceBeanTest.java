package dev.sheldan.abstracto.statistic.emote.service.management;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emote.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmote;
import dev.sheldan.abstracto.statistic.emote.repository.UsedEmoteRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UsedEmoteManagementServiceBeanTest {

    private static final Long EMOTE_ID = 4L;
    private static final Long SERVER_ID = 3L;
    private static final Long COUNT = 5L;

    @InjectMocks
    private UsedEmoteManagementServiceBean testUnit;

    @Mock
    private UsedEmoteRepository usedEmoteRepository;

    @Mock
    private TrackedEmote trackedEmote;

    @Mock
    private UsedEmote usedEmote;

    @Mock
    private AServer server;

    @Captor
    private ArgumentCaptor<UsedEmote> usedEmoteArgumentCaptor;

    @Test
    public void testLoadUsedEmoteForTrackedEmoteToday() {
        setupTrackedEmote();
        when(usedEmoteRepository.findEmoteFromServerToday(EMOTE_ID, SERVER_ID)).thenReturn(Optional.of(usedEmote));
        Optional<UsedEmote> usedEmoteOptional = testUnit.loadUsedEmoteForTrackedEmoteToday(trackedEmote);
        Assert.assertTrue(usedEmoteOptional.isPresent());
        usedEmoteOptional.ifPresent(usedEmote1 ->
            Assert.assertEquals(usedEmote, usedEmote1)
        );
    }

    @Test
    public void testCreateEmoteUsageForToday() {
        setupTrackedEmote();
        testUnit.createEmoteUsageForToday(trackedEmote, COUNT);
        verify(usedEmoteRepository, times(1)).save(usedEmoteArgumentCaptor.capture());
        UsedEmote createdUsedEmote = usedEmoteArgumentCaptor.getValue();
        Assert.assertEquals(COUNT, createdUsedEmote.getAmount());
        Assert.assertEquals(EMOTE_ID, createdUsedEmote.getEmoteId().getEmoteId());
        Assert.assertEquals(SERVER_ID, createdUsedEmote.getEmoteId().getServerId());
    }

    @Test
    public void testPurgeEmoteUsagesSince() {
        setupTrackedEmote();
        testUnit.purgeEmoteUsagesSince(trackedEmote, Instant.EPOCH);
        verify(usedEmoteRepository, times(1)).deleteByEmoteId_EmoteIdAndEmoteId_ServerIdAndEmoteId_UseDateGreaterThan(EMOTE_ID, SERVER_ID, Instant.EPOCH);
    }

    @Test
    public void testLoadEmoteUsagesForServerSince() {
        setupServer();
        when(usedEmoteRepository.getByEmoteId_ServerIdAndEmoteId_UseDateGreaterThan(SERVER_ID, Instant.EPOCH)).thenReturn(Arrays.asList(usedEmote));
        List<UsedEmote> returnedUsedEmotes = testUnit.loadEmoteUsagesForServerSince(server, Instant.EPOCH);
        Assert.assertEquals(1, returnedUsedEmotes.size());
        Assert.assertEquals(usedEmote, returnedUsedEmotes.get(0));
    }

    @Test
    public void testLoadAllEmoteStatsForServerSince() {
        setupServer();
        List<EmoteStatsResult> results = getEmoteStatsResults();
        when(usedEmoteRepository.getEmoteStatsForServerSince(SERVER_ID, Instant.EPOCH)).thenReturn(results);
        List<EmoteStatsResult> returnedResult = testUnit.loadAllEmoteStatsForServerSince(server, Instant.EPOCH);
        Assert.assertEquals(results.size(), returnedResult.size());
        Assert.assertEquals(results, returnedResult);
    }

    @Test
    public void testLoadDeletedEmoteStatsForServerSince() {
        setupServer();
        List<EmoteStatsResult> results = getEmoteStatsResults();
        when(usedEmoteRepository.getDeletedEmoteStatsForServerSince(SERVER_ID, Instant.EPOCH)).thenReturn(results);
        List<EmoteStatsResult> returnedResult = testUnit.loadDeletedEmoteStatsForServerSince(server, Instant.EPOCH);
        Assert.assertEquals(results.size(), returnedResult.size());
        Assert.assertEquals(results, returnedResult);
    }

    @Test
    public void testLoadExternalEmoteStatsForServerSince() {
        setupServer();
        List<EmoteStatsResult> results = getEmoteStatsResults();
        when(usedEmoteRepository.getExternalEmoteStatsForServerSince(SERVER_ID, Instant.EPOCH)).thenReturn(results);
        List<EmoteStatsResult> returnedResult = testUnit.loadExternalEmoteStatsForServerSince(server, Instant.EPOCH);
        Assert.assertEquals(results.size(), returnedResult.size());
        Assert.assertEquals(results, returnedResult);
    }

    @Test
    public void testLoadActiveEmoteStatsForServerSince() {
        setupServer();
        List<EmoteStatsResult> results = getEmoteStatsResults();
        when(usedEmoteRepository.getCurrentlyExistingEmoteStatsForServerSince(SERVER_ID, Instant.EPOCH)).thenReturn(results);
        List<EmoteStatsResult> returnedResult = testUnit.loadActiveEmoteStatsForServerSince(server, Instant.EPOCH);
        Assert.assertEquals(results.size(), returnedResult.size());
        Assert.assertEquals(results, returnedResult);
    }

    public List<EmoteStatsResult> getEmoteStatsResults() {
        EmoteStatsResult emoteStatsResult = Mockito.mock(EmoteStatsResult.class);
        EmoteStatsResult emoteStatsResult2 = Mockito.mock(EmoteStatsResult.class);
        return Arrays.asList(emoteStatsResult, emoteStatsResult2);
    }

    private void setupTrackedEmote() {
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID));
    }

    private void setupServer() {
        when(server.getId()).thenReturn(SERVER_ID);
    }

}
