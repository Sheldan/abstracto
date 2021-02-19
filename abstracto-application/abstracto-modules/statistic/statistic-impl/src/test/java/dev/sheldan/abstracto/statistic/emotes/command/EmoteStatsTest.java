package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsModel;
import dev.sheldan.abstracto.statistic.emotes.model.EmoteStatsResultDisplay;
import dev.sheldan.abstracto.statistic.emotes.service.UsedEmoteService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static dev.sheldan.abstracto.statistic.emotes.command.EmoteStats.EMOTE_STATS_ANIMATED_RESPONSE;
import static dev.sheldan.abstracto.statistic.emotes.command.EmoteStats.EMOTE_STATS_STATIC_RESPONSE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmoteStatsTest {

    @InjectMocks
    private EmoteStats testUnit;

    @Mock
    private UsedEmoteService usedEmoteService;

    @Mock
    private ChannelService channelService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private AServer server;

    @Test
    public void testWithoutParameterStaticEmotes() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        EmoteStatsModel model = Mockito.mock(EmoteStatsModel.class);
        EmoteStatsResultDisplay display = Mockito.mock(EmoteStatsResultDisplay.class);
        when(model.getStaticEmotes()).thenReturn(Arrays.asList(display));
        when(model.areStatsAvailable()).thenReturn(true);
        when(serverManagementService.loadServer(noParameters.getGuild())).thenReturn(server);
        when(usedEmoteService.getActiveEmoteStatsForServerSince(server, Instant.EPOCH)).thenReturn(model);
        when(channelService.sendEmbedTemplateInTextChannelList(EMOTE_STATS_STATIC_RESPONSE, model, noParameters.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        CommandTestUtilities.checkSuccessfulCompletionAsync(testUnit.executeAsync(noParameters));
    }

    @Test
    public void testWithoutParameterAnimatedEmotesEmotes() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        EmoteStatsModel model = Mockito.mock(EmoteStatsModel.class);
        EmoteStatsResultDisplay display = Mockito.mock(EmoteStatsResultDisplay.class);
        when(model.getAnimatedEmotes()).thenReturn(Arrays.asList(display));
        when(model.areStatsAvailable()).thenReturn(true);
        when(serverManagementService.loadServer(noParameters.getGuild())).thenReturn(server);
        when(usedEmoteService.getActiveEmoteStatsForServerSince(server, Instant.EPOCH)).thenReturn(model);
        when(channelService.sendEmbedTemplateInTextChannelList(EMOTE_STATS_ANIMATED_RESPONSE, model, noParameters.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        CommandTestUtilities.checkSuccessfulCompletionAsync(testUnit.executeAsync(noParameters));
    }

    @Test
    public void testWithoutParametersNoResult() {
        CommandContext noParameters = CommandTestUtilities.getNoParameters();
        EmoteStatsModel model = Mockito.mock(EmoteStatsModel.class);
        when(model.areStatsAvailable()).thenReturn(false);
        when(serverManagementService.loadServer(noParameters.getGuild())).thenReturn(server);
        when(usedEmoteService.getActiveEmoteStatsForServerSince(server, Instant.EPOCH)).thenReturn(model);
        when(channelService.sendEmbedTemplateInTextChannelList(eq(EmoteStats.EMOTE_STATS_NO_STATS_AVAILABLE), any(), eq(noParameters.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        CommandTestUtilities.checkSuccessfulCompletionAsync(testUnit.executeAsync(noParameters));
    }

    @Test
    public void testWithParameterStaticEmotes() {
        CommandContext noParameters = CommandTestUtilities.getWithParameters(Arrays.asList(Duration.ofHours(4)));
        EmoteStatsModel model = Mockito.mock(EmoteStatsModel.class);
        EmoteStatsResultDisplay display = Mockito.mock(EmoteStatsResultDisplay.class);
        when(model.getStaticEmotes()).thenReturn(Arrays.asList(display));
        when(model.areStatsAvailable()).thenReturn(true);
        when(serverManagementService.loadServer(noParameters.getGuild())).thenReturn(server);
        when(usedEmoteService.getActiveEmoteStatsForServerSince(eq(server), any(Instant.class))).thenReturn(model);
        when(channelService.sendEmbedTemplateInTextChannelList(EMOTE_STATS_STATIC_RESPONSE, model, noParameters.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        CommandTestUtilities.checkSuccessfulCompletionAsync(testUnit.executeAsync(noParameters));
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatures.EMOTE_TRACKING, testUnit.getFeature());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
