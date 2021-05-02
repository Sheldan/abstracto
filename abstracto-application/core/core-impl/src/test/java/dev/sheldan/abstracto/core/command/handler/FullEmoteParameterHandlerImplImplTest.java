package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.service.EmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FullEmoteParameterHandlerImplImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private FullEmoteParameterHandlerImpl testUnit;

    @Mock
    private EmoteParameterHandlerImpl emoteParameterHandler;

    @Mock
    private EmoteService emoteService;

    @Mock
    private CommandService commandService;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private Emote emote;

    @Mock
    private Message message;

    @Mock
    private AEmote aEmote;

    @Mock
    private Parameter parameter;

    @Mock
    private Parameter parameter2;

    @Mock
    private Command command;

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(FullEmote.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testProperEmoteMention() {
        String input = "test";
        UnparsedCommandParameterPiece piece = getPieceWithValue(input);
        when(commandService.cloneParameter(parameter)).thenReturn(parameter2);
        when(emoteParameterHandler.handle(piece, iterators, parameter2, message, command)).thenReturn(emote);
        when(emoteService.getFakeEmoteFromEmote(emote)).thenReturn(aEmote);
        FullEmote parsed = (FullEmote) testUnit.handle(piece, iterators, parameter, message, command);
        Assert.assertEquals(aEmote, parsed.getFakeEmote());
        Assert.assertEquals(emote, parsed.getEmote());
    }


    @Test
    public void testDefaultEmoteHandling() {
        String input = "test";
        UnparsedCommandParameterPiece piece = getPieceWithValue(input);
        when(commandService.cloneParameter(parameter)).thenReturn(parameter2);
        when(emoteParameterHandler.handle(piece, iterators, parameter2, message, command)).thenReturn(null);
        when(emoteService.getFakeEmote(input)).thenReturn(aEmote);
        FullEmote parsed = (FullEmote) testUnit.handle(piece, iterators, parameter, message, command);
        Assert.assertNull(parsed.getEmote());
        Assert.assertEquals(aEmote, parsed.getFakeEmote());
    }


}
