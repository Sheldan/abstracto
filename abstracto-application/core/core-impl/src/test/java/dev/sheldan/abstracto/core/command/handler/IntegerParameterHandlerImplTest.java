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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IntegerParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private IntegerParameterHandlerImpl testUnit;

    @Mock
    private Parameter parameter;

    @Mock
    private Command command;

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    @Test
    public void testSuccessfulCondition() {
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);

        assertThat(testUnit.handles(Integer.class, unparsedCommandParameterPiece)).isTrue();
    }

    @Test
    public void testWrongCondition() {
        assertThat(testUnit.handles(String.class, unparsedCommandParameterPiece)).isFalse();
    }

    @Test
    public void testSuccessfulParse() {
        assertThat(testUnit.handle(getPieceWithValue("5"), null, parameter, null, command)).isEqualTo(5);
    }

    @Test
    public void testNegativeNumber() {
        assertThat(testUnit.handle(getPieceWithValue("-5"), null, parameter, null, command)).isEqualTo(-5);
    }


    @Test
    public void testDecimal() {
        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue("3.14"), null, parameter, null, command);
        }).isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testTextAsInput() {
        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue("someText"), null, parameter, null, command);
        }).isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testEmptyStringAsInput() {
        assertThatThrownBy(() -> {
            testUnit.handle(getPieceWithValue(""), null, parameter, null, command);
        }).isInstanceOf(NumberFormatException.class);
    }

}
