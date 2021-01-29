package dev.sheldan.abstracto.core.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class OffsetDateTimeAdapter implements JsonDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(jsonElement.getAsString());
        Instant i = Instant.from(ta);
        return OffsetDateTime.ofInstant(i, ZoneId.systemDefault());
    }
}
