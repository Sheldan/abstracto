package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.exception.DurationFormatException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DurationParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private DurationParameterHandlerImpl testUnit;

    @Mock
    private Parameter parameter;

    @Mock
    private Command command;

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    @Test
    public void testSuccessfulCondition() {
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);

        assertThat(testUnit.handles(Duration.class, unparsedCommandParameterPiece)).isTrue();
    }

    @Test
    public void testWrongCondition() {
        assertThat(testUnit.handles(String.class, unparsedCommandParameterPiece)).isFalse();
    }

    @Test
    public void testSimpleParsing() {
        assertThat(testUnit.handle(getPieceWithValue("1m"), null, parameter, null, command)).isEqualTo(Duration.ofMinutes(1));
    }

    @Test
    public void testMoreComplicatedParsing() {
        Duration targetDuration = Duration.ofDays(4).plus(5, ChronoUnit.HOURS).plus(5, ChronoUnit.MINUTES);

        assertThat(testUnit.handle(getPieceWithValue("5h5m4d"), null, parameter, null, command)).isEqualTo(targetDuration);
    }

    @Test
    public void testEmptyStringAsInput() {
        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue(""), null, parameter, null, command);
        }).isInstanceOf(DurationFormatException.class);
    }

}
