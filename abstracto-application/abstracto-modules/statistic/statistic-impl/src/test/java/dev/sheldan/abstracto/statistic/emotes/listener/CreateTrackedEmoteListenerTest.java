package dev.sheldan.abstracto.statistic.emotes.listener;

import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
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
public class CreateTrackedEmoteListenerTest {

    @InjectMocks
    private CreateTrackedEmoteListener testUnit;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Test
    public void testEmoteCreated() {
        Long serverId = 4L;
        Long emoteId = 5L;
        CachedEmote emote = Mockito.mock(CachedEmote.class);
        when(emote.getEmoteId()).thenReturn(emoteId);
        when(emote.getServerId()).thenReturn(serverId);
        testUnit.emoteCreated(emote);
        verify(trackedEmoteManagementService, times(1)).createTrackedEmote(emote);
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
