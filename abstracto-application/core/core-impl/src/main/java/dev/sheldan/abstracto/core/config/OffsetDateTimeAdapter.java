package dev.sheldan.abstracto.core.config;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class OffsetDateTimeAdapter implements JsonDeserializer<OffsetDateTime>, JsonSerializer<OffsetDateTime> {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Override
    public OffsetDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        TemporalAccessor ta = FORMATTER.parse(jsonElement.getAsString());
        Instant i = Instant.from(ta);
        return OffsetDateTime.ofInstant(i, ZoneId.systemDefault());
    }

    @Override
    public JsonElement serialize(OffsetDateTime offsetDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(FORMATTER.format(offsetDateTime));
    }
}
