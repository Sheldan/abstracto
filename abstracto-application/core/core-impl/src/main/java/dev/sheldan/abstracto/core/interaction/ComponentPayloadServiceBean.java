package dev.sheldan.abstracto.core.interaction;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.models.database.ComponentType;
import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ComponentPayloadServiceBean implements ComponentPayloadService {

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private Gson gson;

    @Override
    public ComponentPayload createButtonPayload(String componentId, ButtonPayload payload, String origin, AServer server) {
        String json = gson.toJson(payload);
        return componentPayloadManagementService.createPayload(componentId, json, payload.getClass(), origin, server, ComponentType.BUTTON);
    }

    @Override
    public ComponentPayload createSelectionPayload(String componentId, ButtonPayload payload, String origin, AServer server) {
        String json = gson.toJson(payload);
        return componentPayloadManagementService.createPayload(componentId, json, payload.getClass(), origin, server, ComponentType.SELECTION);
    }
}
