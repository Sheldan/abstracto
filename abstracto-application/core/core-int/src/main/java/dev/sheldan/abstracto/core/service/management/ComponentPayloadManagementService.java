package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.models.template.button.ButtonConfigModel;

import java.util.List;
import java.util.Optional;

public interface ComponentPayloadManagementService {
    ComponentPayload createPayload(String id, String payload, Class payloadType, String buttonOrigin, AServer server);
    ComponentPayload createPayload(ButtonConfigModel buttonConfigModel, AServer server);
    Optional<ComponentPayload> findPayload(String id);
    List<ComponentPayload> findPayloadsOfOriginInServer(String buttonOrigin, AServer server);
    void deletePayload(String id);
}
