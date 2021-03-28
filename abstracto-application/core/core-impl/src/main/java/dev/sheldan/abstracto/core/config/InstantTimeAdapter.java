package dev.sheldan.abstracto.core.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class InstantTimeAdapter implements JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
        TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(jsonElement.getAsString());
        return Instant.from(ta);
    }
}
