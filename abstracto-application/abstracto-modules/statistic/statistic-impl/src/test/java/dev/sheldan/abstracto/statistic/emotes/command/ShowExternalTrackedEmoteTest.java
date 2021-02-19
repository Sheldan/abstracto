package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.statistic.emotes.command.ShowExternalTrackedEmote.SHOW_EXTERNAL_TRACKED_EMOTE_RESPONSE_TEMPLATE_KEY;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShowExternalTrackedEmoteTest {

    @InjectMocks
    private ShowExternalTrackedEmote testUnit;

    @Mock
    private ChannelService channelService;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Test
    public void testShowExternalEmote() {
        TrackedEmote fakeTrackedEmote = Mockito.mock(TrackedEmote.class);
        TrackedEmote actualTrackedEmote = Mockito.mock(TrackedEmote.class);
        when(actualTrackedEmote.getExternal()).thenReturn(true);
        ServerSpecificId trackedEmoteServer = Mockito.mock(ServerSpecificId.class);
        when(fakeTrackedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        when(trackedEmoteManagementService.loadByTrackedEmoteServer(fakeTrackedEmote.getTrackedEmoteId())).thenReturn(actualTrackedEmote);
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(fakeTrackedEmote));
        when(channelService.sendEmbedTemplateInTextChannelList(SHOW_EXTERNAL_TRACKED_EMOTE_RESPONSE_TEMPLATE_KEY, actualTrackedEmote, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> resultFuture = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(resultFuture);
    }

    @Test(expected = AbstractoTemplatedException.class)
    public void testShowNotExternalEmote() {
        TrackedEmote fakeTrackedEmote = Mockito.mock(TrackedEmote.class);
        TrackedEmote actualTrackedEmote = Mockito.mock(TrackedEmote.class);
        when(actualTrackedEmote.getExternal()).thenReturn(false);
        ServerSpecificId trackedEmoteServer = Mockito.mock(ServerSpecificId.class);
        when(fakeTrackedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        when(trackedEmoteManagementService.loadByTrackedEmoteServer(fakeTrackedEmote.getTrackedEmoteId())).thenReturn(actualTrackedEmote);
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(fakeTrackedEmote));
        CompletableFuture<CommandResult> resultFuture = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(resultFuture);
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatures.EMOTE_TRACKING, testUnit.getFeature());
    }

    @Test
    public void testFeatureModeLimitations() {
        Assert.assertTrue(testUnit.getFeatureModeLimitations().contains(EmoteTrackingMode.EXTERNAL_EMOTES));
    }

    @Test
    public void validateCommand() {
        CommandConfigValidator.validateCommandConfiguration(testUnit.getConfiguration());
    }

}
