package dev.sheldan.abstracto.core.interactive.setup.action;

import com.google.gson.*;
import dev.sheldan.abstracto.core.config.CustomJsonDeSerializer;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interactive.DelayedActionConfig;
import dev.sheldan.abstracto.core.interactive.DelayedActionConfigContainer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class DelayedActionDeSerializer implements JsonDeserializer<DelayedActionConfigContainer>, CustomJsonDeSerializer {

    @Override
    public DelayedActionConfigContainer deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        String foundTypeString =  jdc.deserialize(jsonElement.getAsJsonObject().get("type"), String.class);
        Class foundType = null;
        try {
            foundType = Class.forName(foundTypeString);
        } catch (ClassNotFoundException e) {
            throw new AbstractoRunTimeException(String.format("Class %s for de-serializing button payload not found.", foundTypeString));
        }
        DelayedActionConfig foundObjectPayload =  jdc.deserialize(jsonElement.getAsJsonObject().get("payload"), foundType);
        return DelayedActionConfigContainer
                .builder()
                .type(foundType)
                .object(foundObjectPayload)
                .build();
    }

    @Override
    public Class getType() {
        return DelayedActionConfigContainer.class;
    }
}
