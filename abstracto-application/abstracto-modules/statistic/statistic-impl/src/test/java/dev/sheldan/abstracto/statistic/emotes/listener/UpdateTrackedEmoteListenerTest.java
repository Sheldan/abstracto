package dev.sheldan.abstracto.statistic.emotes.listener;

import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import net.dv8tion.jda.api.entities.Emote;
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
        Emote changedEmote = Mockito.mock(Emote.class);
        TrackedEmote trackedEmote = Mockito.mock(TrackedEmote.class);
        when(trackedEmoteManagementService.loadByEmote(changedEmote)).thenReturn(trackedEmote);
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
