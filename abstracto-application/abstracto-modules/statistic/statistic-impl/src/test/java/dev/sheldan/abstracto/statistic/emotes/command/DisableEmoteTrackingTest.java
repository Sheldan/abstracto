package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
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
public class DisableEmoteTrackingTest {

    @InjectMocks
    private DisableEmoteTracking testUnit;

    @Mock
    private TrackedEmoteService trackedEmoteService;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void testDisableAllTracking() {
        CommandContext commandContext = CommandTestUtilities.getNoParameters();
        testUnit.execute(commandContext);
        verify(trackedEmoteService, times(1)).disableEmoteTracking(commandContext.getGuild());
    }

    @Test
    public void testDisableTrackingForOneTrackedEmote() {
        TrackedEmote fakeTrackedEmote = Mockito.mock(TrackedEmote.class);
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(fakeTrackedEmote));
        TrackedEmote actualTrackedEmote = Mockito.mock(TrackedEmote.class);
        TrackedEmoteServer trackedEmoteServer = Mockito.mock(TrackedEmoteServer.class);
        when(fakeTrackedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        when(trackedEmoteManagementService.loadByTrackedEmoteServer(trackedEmoteServer)).thenReturn(actualTrackedEmote);
        testUnit.execute(commandContext);
        verify(trackedEmoteManagementService, times(1)).disableTrackedEmote(actualTrackedEmote);
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatures.EMOTE_TRACKING, testUnit.getFeature());
    }
}
