package dev.sheldan.abstracto.core.interactive.setup.action;

import com.google.gson.*;
import dev.sheldan.abstracto.core.config.CustomJsonSerializer;
import dev.sheldan.abstracto.core.interactive.DelayedActionConfigContainer;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class DelayedActionSerializer implements CustomJsonSerializer, JsonSerializer<DelayedActionConfigContainer> {

    @Override
    public JsonElement serialize(DelayedActionConfigContainer container, Type type, JsonSerializationContext jsc) {

        if(container == null) {
            return null;
        }
        JsonObject messageObj = new JsonObject();
        messageObj.add("type", jsc.serialize(container.getType().getCanonicalName()));
        messageObj.add("payload", jsc.serialize(container.getObject()));
        return messageObj;
    }

    @Override
    public Class getType() {
        return DelayedActionConfigContainer.class;
    }
}
