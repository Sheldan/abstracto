package dev.sheldan.abstracto.statistic.emotes.converter;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsResult;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
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

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmoteStatsConverterTest {
    @InjectMocks
    private EmoteStatsConverter testUnit;

    @Mock
    private BotService botService;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Mock
    private EmoteStatsResult emoteStatsResult;

    @Mock
    private EmoteStatsResult emoteStatsResult2;

    @Mock
    private TrackedEmote trackedEmote;

    @Mock
    private TrackedEmote trackedEmote2;

    @Mock
    private Guild guild;

    private static final Long EMOTE_ID = 4L;
    private static final Long EMOTE_ID_2 = 5L;
    private static final Long SERVER_ID = 6L;

    @Test
    public void testFromEmoteStatsResultsEmpty() {
        EmoteStatsModel result = testUnit.fromEmoteStatsResults(new ArrayList<>());
        Assert.assertEquals(0, result.getStaticEmotes().size());
        Assert.assertEquals(0, result.getAnimatedEmotes().size());
        Assert.assertFalse(result.areStatsAvailable());
    }

    @Test
    public void testFromEmoteStatsExternal() {
        setupResults();
        when(trackedEmote.getExternal()).thenReturn(true);
        when(trackedEmote.getAnimated()).thenReturn(false);
        when(trackedEmote2.getExternal()).thenReturn(true);
        when(trackedEmote2.getAnimated()).thenReturn(true);
        when(botService.getGuildById(SERVER_ID)).thenReturn(guild);
        EmoteStatsModel result = testUnit.fromEmoteStatsResults(Arrays.asList(emoteStatsResult, emoteStatsResult2));

        Assert.assertEquals(1, result.getStaticEmotes().size());
        Assert.assertEquals(trackedEmote, result.getStaticEmotes().get(0).getTrackedEmote());
        Assert.assertNull(result.getStaticEmotes().get(0).getEmote());
        Assert.assertEquals(1, result.getAnimatedEmotes().size());
        Assert.assertNull(result.getAnimatedEmotes().get(0).getEmote());
        Assert.assertEquals(trackedEmote2, result.getAnimatedEmotes().get(0).getTrackedEmote());
        Assert.assertTrue(result.areStatsAvailable());
    }

    @Test
    public void testFromEmoteStatsInternal() {
        setupResults();
        when(trackedEmote.getExternal()).thenReturn(false);
        when(trackedEmote.getAnimated()).thenReturn(false);
        when(trackedEmote.getDeleted()).thenReturn(false);
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID));
        when(trackedEmote2.getExternal()).thenReturn(false);
        when(trackedEmote2.getAnimated()).thenReturn(true);
        when(trackedEmote2.getDeleted()).thenReturn(false);
        when(trackedEmote2.getTrackedEmoteId()).thenReturn(new ServerSpecificId(SERVER_ID, EMOTE_ID_2));
        when(botService.getGuildById(SERVER_ID)).thenReturn(guild);
        Emote emote1 = Mockito.mock(Emote.class);
        when(guild.getEmoteById(EMOTE_ID)).thenReturn(emote1);
        Emote emote2 = Mockito.mock(Emote.class);
        when(guild.getEmoteById(EMOTE_ID_2)).thenReturn(emote2);
        EmoteStatsModel result = testUnit.fromEmoteStatsResults(Arrays.asList(emoteStatsResult, emoteStatsResult2));

        Assert.assertEquals(1, result.getStaticEmotes().size());
        Assert.assertEquals(trackedEmote, result.getStaticEmotes().get(0).getTrackedEmote());
        Assert.assertEquals(emote1, result.getStaticEmotes().get(0).getEmote());
        Assert.assertEquals(1, result.getAnimatedEmotes().size());
        Assert.assertEquals(emote2, result.getAnimatedEmotes().get(0).getEmote());
        Assert.assertEquals(trackedEmote2, result.getAnimatedEmotes().get(0).getTrackedEmote());
        Assert.assertTrue(result.areStatsAvailable());
    }

    @Test
    public void testFromEmoteStatsInternalDeleted() {
        setupResults();
        when(trackedEmote.getExternal()).thenReturn(false);
        when(trackedEmote.getDeleted()).thenReturn(true);
        when(trackedEmote.getAnimated()).thenReturn(false);
        when(trackedEmote2.getExternal()).thenReturn(false);
        when(trackedEmote2.getAnimated()).thenReturn(true);
        when(trackedEmote2.getDeleted()).thenReturn(true);
        when(botService.getGuildById(SERVER_ID)).thenReturn(guild);
        EmoteStatsModel result = testUnit.fromEmoteStatsResults(Arrays.asList(emoteStatsResult, emoteStatsResult2));

        Assert.assertEquals(1, result.getStaticEmotes().size());
        Assert.assertEquals(trackedEmote, result.getStaticEmotes().get(0).getTrackedEmote());
        Assert.assertNull(result.getStaticEmotes().get(0).getEmote());
        Assert.assertEquals(1, result.getAnimatedEmotes().size());
        Assert.assertNull(result.getAnimatedEmotes().get(0).getEmote());
        Assert.assertEquals(trackedEmote2, result.getAnimatedEmotes().get(0).getTrackedEmote());
        Assert.assertTrue(result.areStatsAvailable());
    }


    private void setupResults() {
        when(emoteStatsResult.getEmoteId()).thenReturn(EMOTE_ID);
        when(emoteStatsResult.getServerId()).thenReturn(SERVER_ID);
        when(trackedEmoteManagementService.loadByEmoteId(EMOTE_ID, SERVER_ID)).thenReturn(trackedEmote);
        when(emoteStatsResult2.getEmoteId()).thenReturn(EMOTE_ID_2);
        when(emoteStatsResult2.getServerId()).thenReturn(SERVER_ID);
        when(trackedEmoteManagementService.loadByEmoteId(EMOTE_ID_2, SERVER_ID)).thenReturn(trackedEmote2);
    }
}
