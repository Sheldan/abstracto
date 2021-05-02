package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.exception.DurationFormatException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DurationParameterHandlerImplTest extends AbstractParameterHandlerTest {

    @InjectMocks
    private DurationParameterHandlerImpl testUnit;

    @Mock
    private Parameter parameter;

    @Mock
    private Command command;

    @Mock
    private UnparsedCommandParameterPiece unparsedCommandParameterPiece;

    @Test
    public void testSuccessfulCondition() {
        when(unparsedCommandParameterPiece.getType()).thenReturn(ParameterPieceType.STRING);
        Assert.assertTrue(testUnit.handles(Duration.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class, unparsedCommandParameterPiece));
    }

    @Test
    public void testSimpleParsing() {
        Assert.assertEquals(Duration.ofMinutes(1), testUnit.handle(getPieceWithValue("1m"), null, parameter, null, command));
    }

    @Test
    public void testMoreComplicatedParsing() {
        Duration targetDuration = Duration.ofDays(4).plus(5, ChronoUnit.HOURS).plus(5, ChronoUnit.MINUTES);
        Assert.assertEquals(targetDuration, testUnit.handle(getPieceWithValue("5h5m4d"), null, parameter, null, command));
    }

    @Test(expected = DurationFormatException.class)
    public void testNullInput() {
        testUnit.handle(getPieceWithValue(null), null, parameter, null, command);
    }

    @Test(expected = DurationFormatException.class)
    public void testEmptyStringAsInput() {
        testUnit.handle(getPieceWithValue(""), null, parameter, null, command);
    }

}
