package dev.sheldan.abstracto.statistic.emote.listener;

import dev.sheldan.abstracto.core.models.listener.EmoteCreatedModel;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
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
public class CreateTrackedEmoteListenerTest {

    @InjectMocks
    private CreateTrackedEmoteListener testUnit;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Mock
    private EmoteCreatedModel model;

    private static final Long SERVER_ID = 4L;
    private static final Long EMOTE_ID = 4L;

    @Test
    public void testEmoteCreated() {
        Emote emote = Mockito.mock(Emote.class);
        when(emote.getIdLong()).thenReturn(EMOTE_ID);
        when(model.getEmote()).thenReturn(emote);
        when(model.getServerId()).thenReturn(SERVER_ID);
        testUnit.execute(model);
        verify(trackedEmoteManagementService, times(1)).createTrackedEmote(emote);
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatureDefinition.EMOTE_TRACKING, testUnit.getFeature());
    }

}
