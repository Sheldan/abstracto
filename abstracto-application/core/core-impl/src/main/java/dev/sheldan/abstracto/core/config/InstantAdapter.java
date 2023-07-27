package dev.sheldan.abstracto.core.config;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class InstantAdapter implements JsonDeserializer<Instant>, JsonSerializer<Instant> {

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Override
    public Instant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        TemporalAccessor ta = FORMATTER.parse(jsonElement.getAsString());
        return Instant.from(ta);
    }

    @Override
    public JsonElement serialize(Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(FORMATTER.format(instant));
    }
}
