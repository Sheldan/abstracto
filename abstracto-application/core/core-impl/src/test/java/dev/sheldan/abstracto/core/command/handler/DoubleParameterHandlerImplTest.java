package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DoubleParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private DoubleParameterHandlerImpl testUnit;

    @Mock
    private Parameter parameter;

    @Mock
    private Command command;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(Double.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testSuccessfulParse() {
        Assert.assertEquals(5D, testUnit.handle(getPieceWithValue("5"), null, parameter, null, command));
    }

    @Test
    public void testNegativeNumber() {
        Assert.assertEquals(-5D, testUnit.handle(getPieceWithValue("-5"), null, parameter, null, command));
    }


    public void testDecimal() {
        Assert.assertEquals(3.14D, testUnit.handle(getPieceWithValue("3.14"), null, parameter, null, command));
    }

    @Test(expected = NumberFormatException.class)
    public void testTextAsInput() {
        testUnit.handle(getPieceWithValue("someText"), null, parameter, null, command);
    }

    @Test(expected = NullPointerException.class)
    public void testNullInput() {
        testUnit.handle(getPieceWithValue(null), null, parameter, null, command);
    }

    @Test(expected = NumberFormatException.class)
    public void testEmptyStringAsInput() {
        testUnit.handle(getPieceWithValue(""), null, parameter, null, command);
    }

}
