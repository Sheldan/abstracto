package dev.sheldan.abstracto.statistic.emotes.command.handler;

import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.command.handler.provided.EmoteParameterHandler;
import dev.sheldan.abstracto.statistic.emotes.command.parameter.TrackEmoteParameter;
import dev.sheldan.abstracto.statistic.emotes.command.parameter.handler.TrackedEmoteParameterParameterHandler;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
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
    private Message contextMessage;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private Guild guild;

    @Mock
    private TrackedEmote trackedEmote;

    private static final String WRONG_FORMATTED_INPUT = "input";

    @Test
    public void testHandleIncorrect() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testHandleCorrect() {
        Assert.assertTrue(testUnit.handles(TrackEmoteParameter.class));
    }

    @Test
    public void testHandleWithEmote() {
        when(contextMessage.getGuild()).thenReturn(guild);
        Emote emote = Mockito.mock(Emote.class);
        UnparsedCommandParameterPiece input = UnparsedCommandParameterPiece.builder().value(WRONG_FORMATTED_INPUT).build();
        when(emoteParameterHandler.handle(input, iterators, Emote.class, contextMessage)).thenReturn(emote);
        when(trackedEmoteService.getFakeTrackedEmote(emote, guild)).thenReturn(trackedEmote);
        TrackEmoteParameter parsedEmote = (TrackEmoteParameter) testUnit.handle(input, iterators, TrackedEmote.class, contextMessage);
        Assert.assertEquals(trackedEmote, parsedEmote.getTrackedEmote());
        Assert.assertEquals(emote, parsedEmote.getEmote());
    }

    @Test
    public void testHandleWithId() {
        Long emoteId = 5L;
        when(contextMessage.getGuild()).thenReturn(guild);
        UnparsedCommandParameterPiece input = UnparsedCommandParameterPiece.builder().value(emoteId.toString()).build();
        when(trackedEmoteService.getFakeTrackedEmote(emoteId, guild)).thenReturn(trackedEmote);
        when(emoteParameterHandler.handle(input, iterators, Emote.class, contextMessage)).thenReturn(null);
        TrackEmoteParameter parsedEmote = (TrackEmoteParameter) testUnit.handle(input, iterators, TrackedEmote.class, contextMessage);
        verify(trackedEmoteService, times(0)).getFakeTrackedEmote(any(Emote.class), eq(guild));
        Assert.assertEquals(trackedEmote, parsedEmote.getTrackedEmote());
    }

    @Test(expected = NumberFormatException.class)
    public void testWithIllegalInput() {
        UnparsedCommandParameterPiece input = UnparsedCommandParameterPiece.builder().value(WRONG_FORMATTED_INPUT).build();
        when(emoteParameterHandler.handle(input, iterators, Emote.class, contextMessage)).thenReturn(null);
        testUnit.handle(input, iterators, TrackedEmote.class, contextMessage);
    }

}
