package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.model.TrackedEmoteSynchronizationResult;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.statistic.emotes.command.SyncTrackedEmotes.SYNC_TRACKED_EMOTES_RESULT_RESPONSE;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SyncTrackedEmotesTest {

    @InjectMocks
    private SyncTrackedEmotes testUnit;

    @Mock
    private TrackedEmoteService trackedEmoteService;

    @Mock
    private ChannelService channelService;

    @Test
    public void testExecuteSync() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        TrackedEmoteSynchronizationResult result = Mockito.mock(TrackedEmoteSynchronizationResult.class);
        when(trackedEmoteService.synchronizeTrackedEmotes(commandContext.getGuild())).thenReturn(result);
        when(channelService.sendEmbedTemplateInChannel(SYNC_TRACKED_EMOTES_RESULT_RESPONSE, result, commandContext.getChannel())).thenReturn(CommandTestUtilities.messageFutureList());
        CompletableFuture<CommandResult> asyncResult = testUnit.executeAsync(commandContext);
        CommandTestUtilities.checkSuccessfulCompletionAsync(asyncResult);
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatures.EMOTE_TRACKING, testUnit.getFeature());
    }

}
