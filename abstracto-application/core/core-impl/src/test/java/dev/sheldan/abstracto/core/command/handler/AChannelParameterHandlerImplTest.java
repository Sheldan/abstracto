package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.ChannelService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AChannelParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private AChannelParameterHandlerImpl testUnit;

    @Mock
    private TextChannelParameterHandlerImpl textChannelParameterHandler;

    @Mock
    private ChannelService channelService;

    @Mock
    private CommandParameterIterators iterators;

    @Mock
    private TextChannel channel;

    @Mock
    private Message message;

    @Mock
    private AChannel aChannel;

    @Mock
    private Parameter parameter;

    @Mock
    private Command command;

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    @Test
    public void testSuccessfulCondition() {
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);

        assertThat(testUnit.handles(AChannel.class, unparsedCommandParameterPiece)).isTrue();
    }

    @Test
    public void testWrongCondition() {
        assertThat(testUnit.handles(String.class, unparsedCommandParameterPiece)).isFalse();
    }

    @Test
    public void testProperChannelMention() {
        UnparsedCommandParameterPiece piece = getPiece();
        when(textChannelParameterHandler.handle(piece, iterators, parameter, message, command)).thenReturn(channel);
        when(channelService.getFakeChannelFromTextChannel(channel)).thenReturn(aChannel);

        AChannel parsed = (AChannel) testUnit.handle(piece, iterators, parameter, message, command);

        assertThat(parsed).isEqualTo(aChannel);
    }


}
