package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.test.command.CommandConfigValidator;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.UsedEmoteService;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
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

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PurgeEmoteStatsTest {

    @InjectMocks
    private PurgeEmoteStats testUnit;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Mock
    private UsedEmoteService usedEmoteService;

    @Test
    public void testPurgeEntireTimeLineOfEmote() {
        TrackedEmote fakeTrackedEmote = Mockito.mock(TrackedEmote.class);
        TrackedEmote actualTrackedEmote = Mockito.mock(TrackedEmote.class);
        ServerSpecificId trackedEmoteServer = Mockito.mock(ServerSpecificId.class);
        when(fakeTrackedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        when(trackedEmoteManagementService.loadByTrackedEmoteServer(trackedEmoteServer)).thenReturn(actualTrackedEmote);
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(fakeTrackedEmote));
        testUnit.execute(commandContext);
        verify(usedEmoteService, times(1)).purgeEmoteUsagesSince(actualTrackedEmote, Instant.EPOCH);
    }

    @Test
    public void testPurgeEmoteWithPeriod() {
        TrackedEmote fakeTrackedEmote = Mockito.mock(TrackedEmote.class);
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(fakeTrackedEmote, Duration.ofHours(4)));
        TrackedEmote actualTrackedEmote = Mockito.mock(TrackedEmote.class);
        ServerSpecificId trackedEmoteServer = Mockito.mock(ServerSpecificId.class);
        when(fakeTrackedEmote.getTrackedEmoteId()).thenReturn(trackedEmoteServer);
        when(trackedEmoteManagementService.loadByTrackedEmoteServer(trackedEmoteServer)).thenReturn(actualTrackedEmote);
        testUnit.execute(commandContext);
        verify(usedEmoteService, times(1)).purgeEmoteUsagesSince(eq(actualTrackedEmote), any(Instant.class));
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
