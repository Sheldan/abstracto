package dev.sheldan.abstracto.core.utils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class AbstractoDateUtils {
    public static OffsetDateTime convertInstant(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
