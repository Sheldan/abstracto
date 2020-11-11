package dev.sheldan.abstracto.statistic.emotes.listener;

import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.collections4.bag.HashBag;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmoteTrackingListenerTest {

    @InjectMocks
    private EmoteTrackingListener testUnit;

    @Mock
    private TrackedEmoteService trackedEmoteService;

    @Mock
    private Message message;

    @Mock
    private Emote emote1;

    @Mock
    private Emote emote2;

    @Mock
    private Guild guild;

    private static final Long EMOTE_ID = 4L;

    @Test
    public void testExecuteOneEmote() {
        HashBag<Emote> emotesBag = new HashBag<>();
        emotesBag.add(emote1);
        when(message.getGuild()).thenReturn(guild);
        when(message.getEmotesBag()).thenReturn(emotesBag);
        testUnit.execute(message);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(emote1, guild, 1L);
    }

    @Test
    public void testExecuteOneEmoteMultipleTimes() {
        HashBag<Emote> emotesBag = new HashBag<>();
        when(emote1.getIdLong()).thenReturn(EMOTE_ID);
        when(emote2.getIdLong()).thenReturn(EMOTE_ID);
        emotesBag.add(emote1);
        emotesBag.add(emote2);
        when(message.getGuild()).thenReturn(guild);
        when(message.getEmotesBag()).thenReturn(emotesBag);
        testUnit.execute(message);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(any(Emote.class), eq(guild), eq(2L));
    }

    @Test
    public void testExecuteMultipleEmotes() {
        HashBag<Emote> emotesBag = new HashBag<>();
        when(emote1.getIdLong()).thenReturn(EMOTE_ID);
        when(emote2.getIdLong()).thenReturn(EMOTE_ID + 1);
        emotesBag.add(emote1);
        emotesBag.add(emote2);
        when(message.getGuild()).thenReturn(guild);
        when(message.getEmotesBag()).thenReturn(emotesBag);
        testUnit.execute(message);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(emote1, guild, 1L);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(emote2, guild, 1L);
    }

    @Test
    public void testExecuteNoEmote() {
        when(message.getEmotesBag()).thenReturn(new HashBag<>());
        testUnit.execute(message);
        verify(trackedEmoteService, times(0)).addEmoteToRuntimeStorage(any(), any(), anyLong());
    }


    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatures.EMOTE_TRACKING, testUnit.getFeature());
    }

    @Test
    public void testPriority() {
        Assert.assertEquals(ListenerPriority.LOW, testUnit.getPriority());
    }
}
