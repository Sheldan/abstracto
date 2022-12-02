package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;

public interface ComponentPayloadService {
    ComponentPayload createButtonPayload(String componentId, ButtonPayload payload, String origin, AServer server);
    void updateButtonPayload(String componentId, ButtonPayload payload);
    ComponentPayload createSelectionPayload(String componentId, ButtonPayload payload, String origin, AServer server);
}
