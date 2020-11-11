package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.exception.TrackedEmoteNotFoundException;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.embed.TrackedEmoteServer;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DeleteTrackedEmoteTest {

    @InjectMocks
    private DeleteTrackedEmote testUnit;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Mock
    private TrackedEmoteService trackedEmoteService;

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void testExecuteWithExistingTrackedEmote() {
        TrackedEmote fakedEmote = Mockito.mock(TrackedEmote.class);
        TrackedEmoteServer trackedEmoteServer = new TrackedEmoteServer(1L, 1L);
        when(fakedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        TrackedEmote actualTrackedEmote = Mockito.mock(TrackedEmote.class);
        when(trackedEmoteManagementService.loadByTrackedEmoteServer(trackedEmoteServer)).thenReturn(actualTrackedEmote);
        CommandResult result = testUnit.execute(CommandTestUtilities.getWithParameters(Arrays.asList(fakedEmote)));
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(trackedEmoteService, times(1)).deleteTrackedEmote(actualTrackedEmote);
    }

    @Test(expected = TrackedEmoteNotFoundException.class)
    public void testExecuteNonExistingTrackedEmote() {
        TrackedEmote fakedEmote = Mockito.mock(TrackedEmote.class);
        TrackedEmoteServer trackedEmoteServer = new TrackedEmoteServer(1L, 1L);
        when(fakedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        when(trackedEmoteManagementService.loadByTrackedEmoteServer(trackedEmoteServer)).thenThrow(new TrackedEmoteNotFoundException());
        testUnit.execute(CommandTestUtilities.getWithParameters(Arrays.asList(fakedEmote)));
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatures.EMOTE_TRACKING, testUnit.getFeature());
    }
}
