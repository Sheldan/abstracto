package dev.sheldan.abstracto.statistic.emote.listener;

import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import org.apache.commons.collections4.Bag;
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
    private GuildService guildService;

    @Mock
    private Message message;

    @Mock
    private MessageReceivedModel messageReceivedModel;

    @Mock
    private Emote emote1;

    @Mock
    private Emote emote2;

    @Mock
    private Guild guild;

    private static final Long EMOTE_ID = 4L;
    private static final Long SERVER_ID = 3L;

    @Test
    public void testExecuteOneEmote() {
        Bag<Emote> emotesBag = new HashBag<>();
        emotesBag.add(emote1);
        when(guildService.getGuildById(SERVER_ID)).thenReturn(guild);
        when(messageReceivedModel.getMessage()).thenReturn(message);
        setupMessage();
        when(messageReceivedModel.getServerId()).thenReturn(SERVER_ID);
        when(message.getEmotesBag()).thenReturn(emotesBag);
        testUnit.execute(messageReceivedModel);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(emote1, guild, 1L);
    }

    @Test
    public void testExecuteOneEmoteMultipleTimes() {
        Bag<Emote> emotesBag = new HashBag<>();
        when(emote1.getIdLong()).thenReturn(EMOTE_ID);
        when(emote2.getIdLong()).thenReturn(EMOTE_ID);
        emotesBag.add(emote1);
        emotesBag.add(emote2);
        setupMessage();
        when(guildService.getGuildById(SERVER_ID)).thenReturn(guild);
        when(messageReceivedModel.getServerId()).thenReturn(SERVER_ID);
        when(messageReceivedModel.getMessage()).thenReturn(message);
        when(message.getEmotesBag()).thenReturn(emotesBag);
        testUnit.execute(messageReceivedModel);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(any(Emote.class), eq(guild), eq(2L));
    }

    @Test
    public void testExecuteMultipleEmotes() {
        Bag<Emote> emotesBag = new HashBag<>();
        when(emote1.getIdLong()).thenReturn(EMOTE_ID);
        when(emote2.getIdLong()).thenReturn(EMOTE_ID + 1);
        emotesBag.add(emote1);
        emotesBag.add(emote2);
        setupMessage();
        when(guildService.getGuildById(SERVER_ID)).thenReturn(guild);
        when(messageReceivedModel.getServerId()).thenReturn(SERVER_ID);
        when(messageReceivedModel.getMessage()).thenReturn(message);
        when(message.getEmotesBag()).thenReturn(emotesBag);
        testUnit.execute(messageReceivedModel);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(emote1, guild, 1L);
        verify(trackedEmoteService, times(1)).addEmoteToRuntimeStorage(emote2, guild, 1L);
    }

    @Test
    public void testExecuteNoEmote() {
        when(message.getEmotesBag()).thenReturn(new HashBag<>());
        when(messageReceivedModel.getMessage()).thenReturn(message);
        setupMessage();
        testUnit.execute(messageReceivedModel);
        verify(trackedEmoteService, times(0)).addEmoteToRuntimeStorage(any(Emote.class), any(), anyLong());
    }

    private void setupMessage() {
        when(message.isFromGuild()).thenReturn(true);
        when(message.isWebhookMessage()).thenReturn(false);
        MessageType type = MessageType.DEFAULT;
        when(message.getType()).thenReturn(type);
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatureDefinition.EMOTE_TRACKING, testUnit.getFeature());
    }

}
