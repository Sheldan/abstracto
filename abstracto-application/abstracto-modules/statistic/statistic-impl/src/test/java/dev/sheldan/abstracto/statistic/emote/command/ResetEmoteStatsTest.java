package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ResetEmoteStatsTest {

    @InjectMocks
    private ResetEmoteStats testUnit;

    @Mock
    private TrackedEmoteService trackedEmoteService;

    @Test
    public void testExecute() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        CommandResult result = testUnit.execute(commandContext);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(trackedEmoteService, times(1)).resetEmoteStats(commandContext.getGuild());
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatureDefinition.EMOTE_TRACKING, testUnit.getFeature());
    }
}
