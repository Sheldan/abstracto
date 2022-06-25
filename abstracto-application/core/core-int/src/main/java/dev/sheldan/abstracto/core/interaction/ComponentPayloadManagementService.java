package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.interaction.modal.ModalConfigPayload;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.models.database.ComponentType;
import dev.sheldan.abstracto.core.interaction.button.ButtonConfigModel;

import java.util.List;
import java.util.Optional;

public interface ComponentPayloadManagementService {
    ComponentPayload createPayload(String id, String payload, Class payloadType, String buttonOrigin, AServer server, ComponentType type);
    ComponentPayload createButtonPayload(ButtonConfigModel buttonConfigModel, AServer server);
    ComponentPayload createButtonPayload(ButtonConfigModel buttonConfigModel, Long serverId);
    ComponentPayload createModalPayload(ModalConfigPayload payloadConfig, Long serverId);
    Optional<ComponentPayload> findPayload(String id);
    List<ComponentPayload> findPayloadsOfOriginInServer(String buttonOrigin, AServer server);
    void deletePayload(String id);
    void deletePayloads(List<String> id);
    void deletePayload(ComponentPayload payload);
}
