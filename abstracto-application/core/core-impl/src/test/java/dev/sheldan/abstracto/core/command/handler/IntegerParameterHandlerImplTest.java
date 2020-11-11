package dev.sheldan.abstracto.core.command.handler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IntegerParameterHandlerImplTest {

    @InjectMocks
    private IntegerParameterHandlerImpl testUnit;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(Integer.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testSuccessfulParse() {
        Assert.assertEquals(5, testUnit.handle("5", null, null, null));
    }

    @Test
    public void testNegativeNumber() {
        Assert.assertEquals(-5, testUnit.handle("-5", null, null, null));
    }


    @Test(expected = NumberFormatException.class)
    public void testDecimal() {
        testUnit.handle("3.14", null, null, null);
    }

    @Test(expected = NumberFormatException.class)
    public void testTextAsInput() {
        testUnit.handle("someText", null, null, null);
    }

    @Test(expected = NumberFormatException.class)
    public void testNullInput() {
        testUnit.handle(null, null, null, null);
    }

    @Test(expected = NumberFormatException.class)
    public void testEmptyStringAsInput() {
        testUnit.handle("", null, null, null);
    }

}
