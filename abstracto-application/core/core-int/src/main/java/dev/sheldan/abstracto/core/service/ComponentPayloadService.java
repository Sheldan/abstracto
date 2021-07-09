package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.models.template.button.ButtonPayload;

public interface ComponentPayloadService {
    ComponentPayload createButtonPayload(String componentId, ButtonPayload payload, String origin, AServer server);
    ComponentPayload createSelectionPayload(String componentId, ButtonPayload payload, String origin, AServer server);
}
