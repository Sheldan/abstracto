package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.exception.DurationFormatException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@RunWith(MockitoJUnitRunner.class)
public class DurationParameterHandlerImplTest {

    @InjectMocks
    private DurationParameterHandlerImpl testUnit;

    @Test
    public void testSuccessfulCondition() {
        Assert.assertTrue(testUnit.handles(Duration.class));
    }

    @Test
    public void testWrongCondition() {
        Assert.assertFalse(testUnit.handles(String.class));
    }

    @Test
    public void testSimpleParsing() {
        Assert.assertEquals(Duration.ofMinutes(1), testUnit.handle("1m", null, null, null));
    }

    @Test
    public void testMoreComplicatedParsing() {
        Duration targetDuration = Duration.ofDays(4).plus(5, ChronoUnit.HOURS).plus(5, ChronoUnit.MINUTES);
        Assert.assertEquals(targetDuration, testUnit.handle("5m5h4d", null, null, null));
    }

    @Test(expected = DurationFormatException.class)
    public void testNullInput() {
        testUnit.handle(null, null, null, null);
    }

    @Test(expected = DurationFormatException.class)
    public void testEmptyStringAsInput() {
        testUnit.handle("", null, null, null);
    }

}
