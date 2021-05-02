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
        Assert.assertTrue(testUnit.handles(Integer.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testSuccessfulParse() {
        Assert.assertEquals(5, testUnit.handle(getPieceWithValue("5"), null, parameter, null, command));
    }

    @Test
    public void testNegativeNumber() {
        Assert.assertEquals(-5, testUnit.handle(getPieceWithValue("-5"), null, parameter, null, command));
    }


    @Test(expected = NumberFormatException.class)
    public void testDecimal() {
        testUnit.handle(getPieceWithValue("3.14"), null, parameter, null, command);
    }

    @Test(expected = NumberFormatException.class)
    public void testTextAsInput() {
        testUnit.handle(getPieceWithValue("someText"), null, parameter, null, command);
    }

    @Test(expected = NumberFormatException.class)
    public void testEmptyStringAsInput() {
        testUnit.handle(getPieceWithValue(""), null, parameter, null, command);
    }

}
