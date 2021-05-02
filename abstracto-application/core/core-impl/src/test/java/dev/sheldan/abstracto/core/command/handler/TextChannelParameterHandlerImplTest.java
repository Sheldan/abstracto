package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TextChannelParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private TextChannelParameterHandlerImpl testUnit;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private TextChannel channel;

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

    private static final Long CHANNEL_ID = 111111111111111111L;

    @Test
    public void testSuccessfulCondition() {
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);
        Assert.assertTrue(testUnit.handles(TextChannel.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testProperChannelMention() {
        oneChannelInIterator();
        String input = getChannelMention();
        TextChannel parsed = (TextChannel) testUnit.handle(getPieceWithValue(input), iterators, parameter, null, command);
        Assert.assertEquals(channel, parsed);
    }

    @Test
    public void testChannelMentionById() {
        setupMessage();
        String input = CHANNEL_ID.toString();
        TextChannel parsed = (TextChannel) testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        Assert.assertEquals(channel, parsed);
    }

    @Test(expected = AbstractoTemplatedException.class)
    public void testInvalidChannelName() {
        String input = "test";
        when(message.getGuild()).thenReturn(guild);
        when(guild.getTextChannelsByName(input, true)).thenReturn(new ArrayList<>());
        testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
    }

    @Test(expected = AbstractoTemplatedException.class)
    public void testFoundMultipleChannelsByName() {
        String input = "test";
        TextChannel secondChannel = Mockito.mock(TextChannel.class);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getTextChannelsByName(input, true)).thenReturn(Arrays.asList(channel, secondChannel));
        testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
    }

    @Test
    public void testFindChannelByName() {
        String input = "test";
        when(message.getGuild()).thenReturn(guild);
        when(guild.getTextChannelsByName(input, true)).thenReturn(Arrays.asList(channel));
        TextChannel returnedChannel =  (TextChannel) testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        Assert.assertEquals(channel, returnedChannel);
    }


    private String getChannelMention() {
        return String.format("<#%d>", CHANNEL_ID);
    }

    private void oneChannelInIterator() {
        List<TextChannel> channels = Arrays.asList(channel);
        when(iterators.getChannelIterator()).thenReturn(channels.iterator());
    }

    private void setupMessage()  {
        when(message.getGuild()).thenReturn(guild);
        when(guild.getTextChannelById(CHANNEL_ID)).thenReturn(channel);
    }


}
