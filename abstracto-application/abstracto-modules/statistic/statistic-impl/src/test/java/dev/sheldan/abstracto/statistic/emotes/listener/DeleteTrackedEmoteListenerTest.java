package dev.sheldan.abstracto.statistic.emotes.listener;

import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeleteTrackedEmoteListenerTest {

    @InjectMocks
    private DeleteTrackedEmoteListener testUnit;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Test
    public void testEmoteDeleted() {
        Long serverId = 4L;
        Long emoteId = 5L;
        Emote emote = Mockito.mock(Emote.class);
        Guild guild = Mockito.mock(Guild.class);
        when(guild.getIdLong()).thenReturn(serverId);
        when(emote.getIdLong()).thenReturn(emoteId);
        when(emote.getGuild()).thenReturn(guild);
        testUnit.emoteDeleted(emote);
        verify(trackedEmoteManagementService, times(1)).markAsDeleted(serverId, emoteId);
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
