package dev.sheldan.abstracto.core.command.handler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LongParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private LongParameterHandlerImpl testUnit;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(Long.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testSuccessfulParse() {
        Assert.assertEquals(5L, testUnit.handle(getPieceWithValue("5"), null, null, null));
    }

    @Test
    public void testNegativeNumber() {
        Assert.assertEquals(-5L, testUnit.handle(getPieceWithValue("-5"), null, null, null));
    }

    @Test(expected = NumberFormatException.class)
    public void testDecimal() {
        testUnit.handle(getPieceWithValue("3.14"), null, null, null);
    }

    @Test(expected = NumberFormatException.class)
    public void testTextAsInput() {
        testUnit.handle(getPieceWithValue("someText"), null, null, null);
    }

    @Test(expected = NumberFormatException.class)
    public void testNullInput() {
        testUnit.handle(getPieceWithValue(null), null, null, null);
    }

    @Test(expected = NumberFormatException.class)
    public void testEmptyStringAsInput() {
        testUnit.handle(getPieceWithValue(""), null, null, null);
    }

}
