package dev.sheldan.abstracto.statistic.emote.listener;

import dev.sheldan.abstracto.core.models.listener.EmoteNameUpdatedModel;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
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
    private UpdateTrackedEmoteNameListener testUnit;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Mock
    private EmoteNameUpdatedModel model;

    @Test
    public void testEmoteUpdated() {
        RichCustomEmoji changedEmote = Mockito.mock(RichCustomEmoji.class);
        TrackedEmote trackedEmote = Mockito.mock(TrackedEmote.class);
        when(trackedEmoteManagementService.loadByEmote(changedEmote)).thenReturn(trackedEmote);
        String newValue = "AFTER";
        when(model.getEmote()).thenReturn(changedEmote);
        when(model.getNewValue()).thenReturn(newValue);
        testUnit.execute(model);
        verify(trackedEmoteManagementService, times(1)).changeName(trackedEmote, newValue);
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatureDefinition.EMOTE_TRACKING, testUnit.getFeature());
    }

}
