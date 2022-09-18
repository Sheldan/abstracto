package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emote.model.AvailableTrackedEmote;
import dev.sheldan.abstracto.statistic.emote.model.TrackedEmoteOverview;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.statistic.emote.command.ShowTrackedEmotes.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShowTrackedEmotesTest {

    @InjectMocks
    private ShowTrackedEmotes testUnit;

    @Mock
    private TrackedEmoteService trackedEmoteService;

    @Mock
    private ChannelService channelService;

    @Mock
    private FeatureModeService featureModeService;

    private static final Long SERVER_ID = 4L;

    @Test
    public void testShowTrackedEmotesNoStats() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmoteOverview overview = Mockito.mock(TrackedEmoteOverview.class);
        when(featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(false);
        when(channelService.sendEmbedTemplateInTextChannelList(eq(SHOW_TRACKED_EMOTES_NO_STATS_AVAILABLE), any(), eq(commandContext.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        when(trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), false)).thenReturn(overview);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);
        verifyNoMessage(commandContext, false, true, true, true, true, true, true);
    }

    @Test
    public void testShowTrackedEmotesNoStatsWithShowAll() {
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(Boolean.TRUE));
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmoteOverview overview = Mockito.mock(TrackedEmoteOverview.class);
        when(featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(false);
        when(channelService.sendEmbedTemplateInTextChannelList(eq(SHOW_TRACKED_EMOTES_NO_STATS_AVAILABLE), any(), eq(commandContext.getChannel()))).thenReturn(CommandTestUtilities.messageFutureList());
        when(trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), true)).thenReturn(overview);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);
        verifyNoMessage(commandContext, false, true, true, true, true, true, true);
    }

    @Test
    public void testShowStaticEmotes() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmoteOverview overview = Mockito.mock(TrackedEmoteOverview.class);
        AvailableTrackedEmote staticEmote = Mockito.mock(AvailableTrackedEmote.class);
        when(featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(false);
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_STATIC_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(overview.getStaticEmotes()).thenReturn(Arrays.asList(staticEmote));
        when(trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), false)).thenReturn(overview);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);

        verifyNoMessage(commandContext, true, false, true, true, true, true, true);
    }

    @Test
    public void testShowAnimatedEmotes() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        when(featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(false);
        TrackedEmoteOverview overview = Mockito.mock(TrackedEmoteOverview.class);
        AvailableTrackedEmote animatedEmote = Mockito.mock(AvailableTrackedEmote.class);
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_ANIMATED_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(overview.getAnimatedEmotes()).thenReturn(Arrays.asList(animatedEmote));
        when(trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), false)).thenReturn(overview);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);

        verifyNoMessage(commandContext, true, true, false, true, true, true, true);
    }

    @Test
    public void testShowDeletedStaticEmote() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmoteOverview overview = Mockito.mock(TrackedEmoteOverview.class);
        when(featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(false);
        TrackedEmote animatedEmote = Mockito.mock(TrackedEmote.class);
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_DELETED_STATIC_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(overview.getDeletedStaticEmotes()).thenReturn(Arrays.asList(animatedEmote));
        when(trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), false)).thenReturn(overview);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);

        verifyNoMessage(commandContext, true, true, true, false, true, true, true);
    }

    @Test
    public void testShowDeletedAnimatedEmote() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmoteOverview overview = Mockito.mock(TrackedEmoteOverview.class);
        when(featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(false);
        TrackedEmote animatedEmote = Mockito.mock(TrackedEmote.class);
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_DELETED_ANIMATED_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(overview.getDeletedAnimatedEmotes()).thenReturn(Arrays.asList(animatedEmote));
        when(trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), false)).thenReturn(overview);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);

        verifyNoMessage(commandContext, true, true, true, true, false, true, true);
    }


    @Test
    public void testShowExternalStatic() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmoteOverview overview = Mockito.mock(TrackedEmoteOverview.class);
        when(featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);
        TrackedEmote animatedEmote = Mockito.mock(TrackedEmote.class);
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_EXTERNAL_STATIC_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(overview.getExternalStaticEmotes()).thenReturn(Arrays.asList(animatedEmote));
        when(trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), false)).thenReturn(overview);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);

        verifyNoMessage(commandContext, true, true, true, true, true, false, true);
    }

    @Test
    public void testShowExternalAnimated() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmoteOverview overview = Mockito.mock(TrackedEmoteOverview.class);
        when(featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);
        TrackedEmote animatedEmote = Mockito.mock(TrackedEmote.class);
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_EXTERNAL_ANIMATED_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(overview.getExternalAnimatedEmotes()).thenReturn(Arrays.asList(animatedEmote));
        when(trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), false)).thenReturn(overview);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);

        verifyNoMessage(commandContext, true, true, true, true, true, true, false);
    }

    @Test
    public void testShowAll() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmoteOverview overview = Mockito.mock(TrackedEmoteOverview.class);
        when(featureModeService.featureModeActive(StatisticFeatureDefinition.EMOTE_TRACKING, SERVER_ID, EmoteTrackingMode.EXTERNAL_EMOTES)).thenReturn(true);
        TrackedEmote animatedEmote = Mockito.mock(TrackedEmote.class);
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_STATIC_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_ANIMATED_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_DELETED_STATIC_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_DELETED_ANIMATED_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_EXTERNAL_STATIC_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_TRACKED_EMOTES_EXTERNAL_ANIMATED_RESPONSE, overview, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        when(overview.getExternalAnimatedEmotes()).thenReturn(Arrays.asList(animatedEmote));
        when(overview.getExternalStaticEmotes()).thenReturn(Arrays.asList(animatedEmote));
        AvailableTrackedEmote trackedEmote = Mockito.mock(AvailableTrackedEmote.class);
        when(overview.getStaticEmotes()).thenReturn(Arrays.asList(trackedEmote));
        when(overview.getAnimatedEmotes()).thenReturn(Arrays.asList(trackedEmote));
        when(overview.getDeletedStaticEmotes()).thenReturn(Arrays.asList(animatedEmote));
        when(overview.getDeletedAnimatedEmotes()).thenReturn(Arrays.asList(animatedEmote));

        when(trackedEmoteService.loadTrackedEmoteOverview(commandContext.getGuild(), false)).thenReturn(overview);
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);

        verifyNoMessage(commandContext, true, false, false, false, false, false, false);
    }

    private void verifyNoMessage(CommandContext commandContext, boolean noStats, boolean staticEmote, boolean animatedEmote, boolean deletedStatic, boolean deletedAnimated, boolean externalStatic, boolean externalAnimated) {
        if(noStats) {
            verify(channelService, times(0)).sendEmbedTemplateInTextChannelList(eq(SHOW_TRACKED_EMOTES_NO_STATS_AVAILABLE), any(), eq(commandContext.getChannel()));
        }
        if(staticEmote) {
            verify(channelService, times(0)).sendEmbedTemplateInTextChannelList(eq(SHOW_TRACKED_EMOTES_STATIC_RESPONSE), any(), eq(commandContext.getChannel()));
        }
        if(animatedEmote) {
            verify(channelService, times(0)).sendEmbedTemplateInTextChannelList(eq(SHOW_TRACKED_EMOTES_ANIMATED_RESPONSE), any(), eq(commandContext.getChannel()));
        }
        if(deletedStatic) {
            verify(channelService, times(0)).sendEmbedTemplateInTextChannelList(eq(SHOW_TRACKED_EMOTES_DELETED_STATIC_RESPONSE), any(), eq(commandContext.getChannel()));
        }
        if(deletedAnimated) {
            verify(channelService, times(0)).sendEmbedTemplateInTextChannelList(eq(SHOW_TRACKED_EMOTES_DELETED_ANIMATED_RESPONSE), any(), eq(commandContext.getChannel()));
        }
        if(externalStatic) {
            verify(channelService, times(0)).sendEmbedTemplateInTextChannelList(eq(SHOW_TRACKED_EMOTES_EXTERNAL_STATIC_RESPONSE), any(), eq(commandContext.getChannel()));
        }
        if(externalAnimated) {
            verify(channelService, times(0)).sendEmbedTemplateInTextChannelList(eq(SHOW_TRACKED_EMOTES_EXTERNAL_ANIMATED_RESPONSE), any(), eq(commandContext.getChannel()));
        }
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatureDefinition.EMOTE_TRACKING, testUnit.getFeature());
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }


}
