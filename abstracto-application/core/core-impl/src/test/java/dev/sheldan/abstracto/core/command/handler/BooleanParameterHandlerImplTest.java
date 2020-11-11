package dev.sheldan.abstracto.core.command.handler;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BooleanParameterHandlerImplTest {

    @InjectMocks
    private BooleanParameterHandlerImpl testUnit;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(Boolean.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testTrueParsing() {
        Assert.assertTrue((Boolean)testUnit.handle("true", null, null, null));
    }

    @Test
    public void testAnyOtherText() {
        Assert.assertFalse((Boolean)testUnit.handle("test", null, null, null));
    }

    @Test
    public void testNullInput() {
        Assert.assertFalse((Boolean)testUnit.handle(null, null, null, null));
    }

    @Test
    public void testEmptyStringAsInput() {
        Assert.assertFalse((Boolean)testUnit.handle("", null, null, null));
    }

}
