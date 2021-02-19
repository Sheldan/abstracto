package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BooleanParameterHandlerImplTest extends AbstractParameterHandlerTest {

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
        UnparsedCommandParameterPiece piece = getPieceWithValue("true");
        Assert.assertTrue((Boolean)testUnit.handle(piece, null, null, null));
    }

    @Test
    public void testAnyOtherText() {
        UnparsedCommandParameterPiece piece = getPieceWithValue("test");
        Assert.assertFalse((Boolean)testUnit.handle(piece, null, null, null));
    }

    @Test
    public void testNullInput() {
        UnparsedCommandParameterPiece piece = getPieceWithValue(null);
        Assert.assertFalse((Boolean)testUnit.handle(piece, null, null, null));
    }

    @Test
    public void testEmptyStringAsInput() {
        UnparsedCommandParameterPiece piece = getPieceWithValue("");
        Assert.assertFalse((Boolean)testUnit.handle(piece, null, null, null));
    }

}
