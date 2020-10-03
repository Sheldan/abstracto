package dev.sheldan.abstracto.core.command.handler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DoubleParameterHandlerTest {

    @InjectMocks
    private DoubleParameterHandler testUnit;

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
        Assert.assertEquals(5D, testUnit.handle("5", null, null, null));
    }

    @Test
    public void testNegativeNumber() {
        Assert.assertEquals(-5D, testUnit.handle("-5", null, null, null));
    }


    public void testDecimal() {
        Assert.assertEquals(3.14D, testUnit.handle("3.14", null, null, null));
    }

    @Test(expected = NumberFormatException.class)
    public void testTextAsInput() {
        testUnit.handle("someText", null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullInput() {
        testUnit.handle(null, null, null, null);
    }

    @Test(expected = NumberFormatException.class)
    public void testEmptyStringAsInput() {
        testUnit.handle("", null, null, null);
    }

}
