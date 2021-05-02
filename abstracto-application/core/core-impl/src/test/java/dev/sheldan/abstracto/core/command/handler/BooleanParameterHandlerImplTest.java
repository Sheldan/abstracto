package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
        Assert.assertTrue(testUnit.handles(Boolean.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testTrueParsing() {
        UnparsedCommandParameterPiece piece = getPieceWithValue("true");
        Assert.assertTrue((Boolean)testUnit.handle(piece, null, parameter, null, command));
    }

    @Test
    public void testAnyOtherText() {
        UnparsedCommandParameterPiece piece = getPieceWithValue("test");
        Assert.assertFalse((Boolean)testUnit.handle(piece, null, parameter, null, command));
    }

    @Test
    public void testNullInput() {
        UnparsedCommandParameterPiece piece = getPieceWithValue(null);
        Assert.assertFalse((Boolean)testUnit.handle(piece, null, parameter, null, command));
    }

    @Test
    public void testEmptyStringAsInput() {
        UnparsedCommandParameterPiece piece = getPieceWithValue("");
        Assert.assertFalse((Boolean)testUnit.handle(piece, null, parameter, null, command));
    }

}
