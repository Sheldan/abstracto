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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BooleanParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private BooleanParameterHandlerImpl testUnit;

    @Mock
    private Parameter parameter;

    @Mock
    private Command command;

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    @Test
    public void testSuccessfulCondition() {
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);
        assertThat(testUnit.handles(Boolean.class, unparsedCommandParameterPiece)).isTrue();
    }

    @Test
    public void testWrongCondition() {
        assertThat(testUnit.handles(String.class, unparsedCommandParameterPiece)).isFalse();
    }

    @Test
    public void testTrueParsing() {
        UnparsedCommandParameterPiece piece = getPieceWithValue("true");
        assertThat((Boolean)testUnit.handle(piece, null, parameter, null, command)).isTrue();
    }

    @Test
    public void testAnyOtherText() {
        UnparsedCommandParameterPiece piece = getPieceWithValue("test");
        assertThat((Boolean)testUnit.handle(piece, null, parameter, null, command)).isFalse();
    }

    @Test
    public void testEmptyStringAsInput() {
        UnparsedCommandParameterPiece piece = getPieceWithValue("");
        assertThat((Boolean)testUnit.handle(piece, null, parameter, null, command)).isFalse();
    }

}
