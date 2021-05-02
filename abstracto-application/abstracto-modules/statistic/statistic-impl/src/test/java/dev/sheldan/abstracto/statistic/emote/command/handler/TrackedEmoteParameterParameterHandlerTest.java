package dev.sheldan.abstracto.statistic.emote.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.command.handler.provided.EmoteParameterHandler;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.statistic.emote.command.parameter.TrackEmoteParameter;
import dev.sheldan.abstracto.statistic.emote.command.parameter.handler.TrackedEmoteParameterParameterHandler;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TrackedEmoteParameterParameterHandlerTest {

    @InjectMocks
    private TrackedEmoteParameterParameterHandler testUnit;

    @Mock
    private EmoteParameterHandler emoteParameterHandler;

    @Mock
    private TrackedEmoteService trackedEmoteService;

    @Mock
    private CommandService commandService;

    @Mock
    private Message contextMessage;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private Guild guild;

    @Mock
    private TrackedEmote trackedEmote;

    @Mock
    private Parameter parameter;

    @Mock
    private Parameter parameter2;

    @Mock
    private Command command;

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    private static final String WRONG_FORMATTED_INPUT = "input";

    @Test
    public void testHandleIncorrect() {
        Assert.assertFalse(testUnit.handles(String.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testHandleCorrect() {
        Assert.assertTrue(testUnit.handles(TrackEmoteParameter.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testHandleWithEmote() {
        when(contextMessage.getGuild()).thenReturn(guild);
        Emote emote = Mockito.mock(Emote.class);
        UnparsedCommandParameterPiece input = Mockito.mock(UnparsedCommandParameterPiece.class);
        when(commandService.cloneParameter(parameter)).thenReturn(parameter2);
        when(emoteParameterHandler.handle(input, iterators, parameter2, contextMessage, command)).thenReturn(emote);
        when(trackedEmoteService.getFakeTrackedEmote(emote, guild)).thenReturn(trackedEmote);
        TrackEmoteParameter parsedEmote = (TrackEmoteParameter) testUnit.handle(input, iterators, parameter, contextMessage, command);
        Assert.assertEquals(trackedEmote, parsedEmote.getTrackedEmote());
        Assert.assertEquals(emote, parsedEmote.getEmote());
    }

    @Test
    public void testHandleWithId() {
        Long emoteId = 5L;
        when(contextMessage.getGuild()).thenReturn(guild);
        UnparsedCommandParameterPiece input = Mockito.mock(UnparsedCommandParameterPiece.class);
        when(input.getValue()).thenReturn(emoteId.toString());
        when(trackedEmoteService.getFakeTrackedEmote(emoteId, guild)).thenReturn(trackedEmote);
        when(commandService.cloneParameter(parameter)).thenReturn(parameter2);
        when(emoteParameterHandler.handle(input, iterators, parameter2, contextMessage, command)).thenReturn(null);
        TrackEmoteParameter parsedEmote = (TrackEmoteParameter) testUnit.handle(input, iterators, parameter, contextMessage, command);
        verify(trackedEmoteService, times(0)).getFakeTrackedEmote(any(Emote.class), eq(guild));
        Assert.assertEquals(trackedEmote, parsedEmote.getTrackedEmote());
    }

    @Test(expected = NumberFormatException.class)
    public void testWithIllegalInput() {
        UnparsedCommandParameterPiece input = Mockito.mock(UnparsedCommandParameterPiece.class);
        when(input.getValue()).thenReturn(WRONG_FORMATTED_INPUT);
        when(commandService.cloneParameter(parameter)).thenReturn(parameter2);
        when(emoteParameterHandler.handle(input, iterators, parameter2, contextMessage, command)).thenReturn(null);
        testUnit.handle(input, iterators, parameter, contextMessage, command);
    }

}
