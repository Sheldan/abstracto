package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DoubleParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private DoubleParameterHandlerImpl testUnit;

    @Mock
    private Parameter parameter;

    @Mock
    private Command command;

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    @Test
    public void testSuccessfulCondition() {
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);
        assertThat(testUnit.handles(Double.class, unparsedCommandParameterPiece)).isTrue();
    }

    @Test
    public void testWrongCondition() {
        assertThat(testUnit.handles(String.class, unparsedCommandParameterPiece)).isFalse();
    }

    @Test
    public void testSuccessfulParse() {
        assertThat(testUnit.handle(getPieceWithValue("5"), null, parameter, null, command)).isEqualTo(5D);
    }

    @Test
    public void testNegativeNumber() {
        assertThat(testUnit.handle(getPieceWithValue("-50"), null, parameter, null, command)).isEqualTo(-50D);
    }


    @Test
    public void testDecimal() {
        assertThat(testUnit.handle(getPieceWithValue("3.14"), null, parameter, null, command)).isEqualTo(3.14D);
    }

    @Test
    public void testTextAsInput() {
        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue("someText"), null, parameter, null, command);
        }).isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testNullInput() {
        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue(null), null, parameter, null, command);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testEmptyStringAsInput() {
        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue(""), null, parameter, null, command);
        }).isInstanceOf(NumberFormatException.class);
    }

}
