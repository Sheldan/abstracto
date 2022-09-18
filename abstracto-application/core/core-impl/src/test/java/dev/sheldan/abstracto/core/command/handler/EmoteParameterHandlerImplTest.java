package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmoteParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private EmoteParameterHandlerImpl testUnit;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private RichCustomEmoji emote;

    @Mock
    private Message message;

    @Mock
    private Guild guild;

    @Mock
    private Parameter parameter;

    @Mock
    private Command command;

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    private static final Long EMOTE_ID = 111111111111111111L;
    private static final String EMOTE_NAME = "test";

    @Test
    public void testSuccessfulCondition() {
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);
        Assert.assertTrue(testUnit.handles(CustomEmoji.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testProperEmoteMention() {
        oneEmoteInIterator();
        String input = getEmoteMention();
        CustomEmoji parsed = (CustomEmoji) testUnit.handle(getPieceWithValue(input), iterators, parameter, null, command);
        Assert.assertEquals(parsed, emote);
    }

    @Test
    public void testEmoteById() {
        setupMessage();
        String input = EMOTE_ID.toString();
        CustomEmoji parsed = (CustomEmoji) testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        Assert.assertEquals(parsed, emote);
    }

    @Test
    public void testInvalidEmoteMention() {
        Assert.assertNull(testUnit.handle(getPieceWithValue("test"), null, parameter, null, command));
    }

    private String getEmoteMention() {
        return String.format("<:%s:%d>", EMOTE_NAME, EMOTE_ID);
    }

    private void oneEmoteInIterator() {
        List<CustomEmoji> emotes = Arrays.asList(emote);
        when(iterators.getEmoteIterator()).thenReturn(emotes.iterator());
    }

    private void setupMessage()  {
        when(message.getGuild()).thenReturn(guild);
        when(guild.getEmojiById(EMOTE_ID)).thenReturn(emote);
    }

}
