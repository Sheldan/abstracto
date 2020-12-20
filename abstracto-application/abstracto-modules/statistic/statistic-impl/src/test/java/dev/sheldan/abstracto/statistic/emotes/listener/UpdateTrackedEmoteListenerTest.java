package dev.sheldan.abstracto.statistic.emotes.listener;

import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateTrackedEmoteListenerTest {

    @InjectMocks
    private UpdateTrackedEmoteListener testUnit;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Test
    public void testEmoteUpdated() {
        Long serverId = 1L;
        Long emoteId = 2L;
        CachedEmote changedEmote = Mockito.mock(CachedEmote.class);
        when(changedEmote.getServerId()).thenReturn(serverId);
        when(changedEmote.getEmoteId()).thenReturn(emoteId);
        TrackedEmote trackedEmote = Mockito.mock(TrackedEmote.class);
        when(trackedEmoteManagementService.loadByEmoteId(emoteId, serverId)).thenReturn(trackedEmote);
        String newValue = "AFTER";
        testUnit.emoteUpdated(changedEmote, "BEFORE", newValue);
        verify(trackedEmoteManagementService, times(1)).changeName(trackedEmote, newValue);
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatures.EMOTE_TRACKING, testUnit.getFeature());
    }

    @Test
    public void testPriority() {
        Assert.assertEquals(ListenerPriority.MEDIUM, testUnit.getPriority());
    }
}
