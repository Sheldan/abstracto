package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.service.EmoteService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
    private RichCustomEmoji emote;

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
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);
        assertThat(testUnit.handles(FullEmote.class, unparsedCommandParameterPiece)).isTrue();
    }

    @Test
    public void testWrongCondition() {
        assertThat(testUnit.handles(String.class, unparsedCommandParameterPiece)).isFalse();
    }

    @Test
    public void testProperEmoteMention() {
        String input = "test";
        UnparsedCommandParameterPiece piece = getPieceWithValue(input);
        when(commandService.cloneParameter(parameter)).thenReturn(parameter2);
        when(emoteParameterHandler.handle(piece, iterators, parameter2, message, command)).thenReturn(emote);
        when(emoteService.getFakeEmoteFromEmote(emote)).thenReturn(aEmote);
        FullEmote parsed = (FullEmote) testUnit.handle(piece, iterators, parameter, message, command);
        assertThat(parsed.getFakeEmote()).isEqualTo(aEmote);
        assertThat(parsed.getEmote()).isEqualTo(emote);
    }


    @Test
    public void testDefaultEmoteHandling() {
        String input = "test";
        UnparsedCommandParameterPiece piece = getPieceWithValue(input);
        when(commandService.cloneParameter(parameter)).thenReturn(parameter2);
        when(emoteParameterHandler.handle(piece, iterators, parameter2, message, command)).thenReturn(null);
        when(emoteService.getFakeEmote(input)).thenReturn(aEmote);
        FullEmote parsed = (FullEmote) testUnit.handle(piece, iterators, parameter, message, command);
        assertThat(parsed.getEmote()).isNull();
        assertThat(parsed.getFakeEmote()).isEqualTo(aEmote);
    }


}
