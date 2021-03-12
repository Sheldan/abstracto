package dev.sheldan.abstracto.statistic.emote.listener;

import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Guild;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmoteTrackingListenerTest {

    @InjectMocks
    private EmoteTrackingListener testUnit;

    @Mock
    private TrackedEmoteService trackedEmoteService;

    @Mock
    private GuildService guildService;

    @Mock
    private CachedMessage message;

    @Mock
    private CachedEmote emote1;

    @Mock
    private CachedEmote emote2;

    @Mock
    private Guild guild;

    private static final Long EMOTE_ID = 4L;
    private static final Long SERVER_ID = 3L;

    @Test
    public void testExecuteOneEmote() {
        List<CachedEmote> emotesBag = new ArrayList<>();
        emotesBag.add(emote1);
        when(guildService.getGuildById(SERVER_ID)).thenReturn(guild);
        when(message.getServerId()).thenReturn(SERVER_ID);
        when(message.getEmotes()).thenReturn(emotesBag);
        testUnit.execute(message);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(emote1, guild, 1L);
    }

    @Test
    public void testExecuteOneEmoteMultipleTimes() {
        List<CachedEmote> emotesBag = new ArrayList<>();
        when(emote1.getEmoteId()).thenReturn(EMOTE_ID);
        when(emote2.getEmoteId()).thenReturn(EMOTE_ID);
        emotesBag.add(emote1);
        emotesBag.add(emote2);
        when(guildService.getGuildById(SERVER_ID)).thenReturn(guild);
        when(message.getServerId()).thenReturn(SERVER_ID);
        when(message.getEmotes()).thenReturn(emotesBag);
        testUnit.execute(message);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(any(CachedEmote.class), eq(guild), eq(2L));
    }

    @Test
    public void testExecuteMultipleEmotes() {
        List<CachedEmote> emotesBag = new ArrayList<>();
        when(emote1.getEmoteId()).thenReturn(EMOTE_ID);
        when(emote2.getEmoteId()).thenReturn(EMOTE_ID + 1);
        emotesBag.add(emote1);
        emotesBag.add(emote2);
        when(guildService.getGuildById(SERVER_ID)).thenReturn(guild);
        when(message.getServerId()).thenReturn(SERVER_ID);
        when(message.getEmotes()).thenReturn(emotesBag);
        testUnit.execute(message);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(emote1, guild, 1L);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(emote2, guild, 1L);
    }

    @Test
    public void testExecuteNoEmote() {
        when(message.getEmotes()).thenReturn(new ArrayList<>());
        testUnit.execute(message);
        verify(trackedEmoteService, times(0)).addEmoteToRuntimeStorage(any(), any(), anyLong());
    }


    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatureDefinition.EMOTE_TRACKING, testUnit.getFeature());
    }

}
