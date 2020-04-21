package dev.sheldan.abstracto.core.utils;


import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ParseUtilsTest {

    @Test
    public void oneDay() {
        Duration duration = ParseUtils.parseDuration("1d");
        assertEquals(Duration.ofDays(1), duration);
    }

    @Test
    public void twoWeeks() {
        Duration duration = ParseUtils.parseDuration("2w");
        assertEquals(Duration.ofDays(14), duration);
    }

    @Test
    public void aDayWithMinutes() {
        Duration duration = ParseUtils.parseDuration("1d3m");
        assertEquals(Duration.ofDays(1).plus(Duration.ofMinutes(3)), duration);
    }

    @Test
    public void allTimeFormats() {
        Duration duration = ParseUtils.parseDuration("2w3d4h2m1s");
        assertEquals(Duration.ofDays(17).plus(Duration.ofHours(4)).plus(Duration.ofMinutes(2)).plus(Duration.ofSeconds(1)), duration);
    }

    @Test
    public void overFlowingTimeFormats() {
        Duration duration = ParseUtils.parseDuration("70s");
        assertEquals(Duration.ofMinutes(1).plus(Duration.ofSeconds(10)), duration);
    }

    @Test(expected = AbstractoRunTimeException.class)
    public void invalidTimeFormat() {
        ParseUtils.parseDuration("1k");
    }
}
