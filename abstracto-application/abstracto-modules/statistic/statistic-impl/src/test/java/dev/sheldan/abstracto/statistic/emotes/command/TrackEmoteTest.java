package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.exception.IncorrectParameterException;
import dev.sheldan.abstracto.core.command.exception.IncorrectParameterTypeException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.exception.IncorrectFeatureModeException;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.test.command.CommandTestUtilities;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.command.parameter.TrackEmoteParameter;
import dev.sheldan.abstracto.statistic.emotes.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.embed.TrackedEmoteServer;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import net.dv8tion.jda.api.entities.Emote;
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
public class TrackEmoteTest {

    @InjectMocks
    private TrackEmote testUnit;

    @Mock
    private TrackedEmoteService trackedEmoteService;

    @Mock
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Mock
    private EmoteService emoteService;

    @Mock
    private FeatureModeService featureModeService;

    @Mock
    private TrackEmoteParameter trackEmoteParameter;

    private static final Long EMOTE_ID = 4L;
    private static final Long SERVER_ID = 5L;

    @Test(expected = InsufficientParametersException.class)
    public void testTooLittleParameters() {
        CommandTestUtilities.executeNoParametersTest(testUnit);
    }

    @Test(expected = IncorrectParameterTypeException.class)
    public void testIncorrectParameterType() {
        CommandTestUtilities.executeWrongParametersTest(testUnit);
    }

    @Test
    public void testReTrackTrackedEmote(){
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(trackEmoteParameter));
        when(trackedEmoteManagementService.trackedEmoteExists(EMOTE_ID, SERVER_ID)).thenReturn(true);
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmote trackedEmote = Mockito.mock(TrackedEmote.class);
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new TrackedEmoteServer(EMOTE_ID, SERVER_ID));
        when(trackedEmoteManagementService.loadByEmoteId(EMOTE_ID, SERVER_ID)).thenReturn(trackedEmote);
        when(trackEmoteParameter.getTrackedEmote()).thenReturn(trackedEmote);
        CommandResult result = testUnit.execute(commandContext);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(trackedEmoteManagementService, times(1)).enableTrackedEmote(trackedEmote);
    }

    @Test(expected = IncorrectFeatureModeException.class)
    public void testTrackExternalWithExternalDisabled(){
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(trackEmoteParameter));
        when(trackedEmoteManagementService.trackedEmoteExists(EMOTE_ID, SERVER_ID)).thenReturn(false);
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmote trackedEmote = Mockito.mock(TrackedEmote.class);
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new TrackedEmoteServer(EMOTE_ID, SERVER_ID));
        Emote emoteToTrack = Mockito.mock(Emote.class);
        when(trackEmoteParameter.getEmote()).thenReturn(emoteToTrack);
        when(trackEmoteParameter.getTrackedEmote()).thenReturn(trackedEmote);
        when(emoteService.emoteIsFromGuild(emoteToTrack, commandContext.getGuild())).thenReturn(false);
        doThrow(new IncorrectFeatureModeException(null, null)).when(featureModeService).validateActiveFeatureMode(SERVER_ID, StatisticFeatures.EMOTE_TRACKING, EmoteTrackingMode.EXTERNAL_EMOTES);
        testUnit.execute(commandContext);
    }

    @Test
    public void testTrackExternalWithExternalEnabled(){
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(trackEmoteParameter));
        when(trackedEmoteManagementService.trackedEmoteExists(EMOTE_ID, SERVER_ID)).thenReturn(false);
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmote trackedEmote = Mockito.mock(TrackedEmote.class);
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new TrackedEmoteServer(EMOTE_ID, SERVER_ID));
        Emote emoteToTrack = Mockito.mock(Emote.class);
        when(trackEmoteParameter.getEmote()).thenReturn(emoteToTrack);
        when(trackEmoteParameter.getTrackedEmote()).thenReturn(trackedEmote);
        when(emoteService.emoteIsFromGuild(emoteToTrack, commandContext.getGuild())).thenReturn(false);
        CommandResult result = testUnit.execute(commandContext);
        CommandTestUtilities.checkSuccessfulCompletion(result);
        verify(trackedEmoteService, times(1)).createFakeTrackedEmote(emoteToTrack, commandContext.getGuild(), true);
        verify(featureModeService, times(1)).validateActiveFeatureMode(SERVER_ID, StatisticFeatures.EMOTE_TRACKING, EmoteTrackingMode.EXTERNAL_EMOTES);
    }

    @Test(expected = IncorrectParameterException.class)
    public void testTrackExternalIncorrectParameter(){
        CommandContext commandContext = CommandTestUtilities.getWithParameters(Arrays.asList(trackEmoteParameter));
        when(trackedEmoteManagementService.trackedEmoteExists(EMOTE_ID, SERVER_ID)).thenReturn(false);
        when(commandContext.getGuild().getIdLong()).thenReturn(SERVER_ID);
        TrackedEmote trackedEmote = Mockito.mock(TrackedEmote.class);
        when(trackedEmote.getTrackedEmoteId()).thenReturn(new TrackedEmoteServer(EMOTE_ID, SERVER_ID));
        when(trackEmoteParameter.getTrackedEmote()).thenReturn(trackedEmote);
        testUnit.execute(commandContext);
    }

    @Test
    public void testFeature() {
        Assert.assertEquals(StatisticFeatures.EMOTE_TRACKING, testUnit.getFeature());
    }
}
