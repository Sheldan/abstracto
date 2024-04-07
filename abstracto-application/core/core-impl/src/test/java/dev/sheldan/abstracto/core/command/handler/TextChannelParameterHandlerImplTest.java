package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
        assertThat(testUnit.handles(TextChannel.class, unparsedCommandParameterPiece)).isTrue();
    }

    @Test
    public void testWrongCondition() {
        assertThat(testUnit.handles(String.class, unparsedCommandParameterPiece)).isFalse();
    }

    @Test
    public void testProperChannelMention() {
        oneChannelInIterator();
        String input = getChannelMention();
        TextChannel parsed = (TextChannel) testUnit.handle(getPieceWithValue(input), iterators, parameter, null, command);
        assertThat(parsed).isEqualTo(channel);
    }

    @Test
    public void testChannelMentionById() {
        setupMessage();
        String input = CHANNEL_ID.toString();
        TextChannel parsed = (TextChannel) testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        assertThat(parsed).isEqualTo(channel);
    }

    @Test
    public void testInvalidChannelName() {
        String input = "test";
        when(message.getGuild()).thenReturn(guild);
        when(guild.getTextChannelsByName(input, true)).thenReturn(new ArrayList<>());
        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        }).isInstanceOf(AbstractoTemplatedException.class);
    }

    @Test
    public void testFoundMultipleChannelsByName() {
        String input = "test";
        TextChannel secondChannel = Mockito.mock(TextChannel.class);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getTextChannelsByName(input, true)).thenReturn(Arrays.asList(channel, secondChannel));
        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        }).isInstanceOf(AbstractoTemplatedException.class);
    }

    @Test
    public void testFindChannelByName() {
        String input = "test";
        when(message.getGuild()).thenReturn(guild);
        when(guild.getTextChannelsByName(input, true)).thenReturn(Arrays.asList(channel));
        TextChannel returnedChannel =  (TextChannel) testUnit.handle(getPieceWithValue(input), null, parameter, message, command);
        assertThat(returnedChannel).isEqualTo(channel);
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
