package dev.sheldan.abstracto.core.service.management;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.menu.SelectMenuConfigModel;
import dev.sheldan.abstracto.core.interaction.modal.ModalConfigPayload;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.models.database.ComponentType;
import dev.sheldan.abstracto.core.interaction.button.ButtonConfigModel;
import dev.sheldan.abstracto.core.repository.ComponentPayloadRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class ComponentPayloadManagementServiceBean implements ComponentPayloadManagementService {

    @Autowired
    private ComponentPayloadRepository repository;

    @Autowired
    private Gson gson;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public ComponentPayload createPayload(String id, String payload, Class payloadType, String origin, AServer server, ComponentType componentType) {
        ComponentPayload componentPayload = ComponentPayload
                .builder()
                .origin(origin)
                .id(id)
                .payload(payload)
                .payloadType(payloadType.getTypeName())
                .server(server)
                .type(componentType)
                .build();
        return repository.save(componentPayload);
    }

    @Override
    public void updatePayload(String id, String payload) {
        findPayload(id).ifPresent(componentPayload -> componentPayload.setPayload(payload));
    }

    @Override
    public ComponentPayload createButtonPayload(ButtonConfigModel buttonConfigModel, AServer server) {
        String payload = gson.toJson(buttonConfigModel.getButtonPayload());
        return createPayload(buttonConfigModel.getButtonId(), payload, buttonConfigModel.getPayloadType(), buttonConfigModel.getOrigin(), server, ComponentType.BUTTON);
    }

    @Override
    public ComponentPayload createButtonPayload(ButtonConfigModel buttonConfigModel, Long serverId) {
        AServer server;
        if(serverId != null) {
            server = serverManagementService.loadOrCreate(serverId);
        } else {
            server = null;
        }
        return createButtonPayload(buttonConfigModel, server);
    }

    @Override
    public ComponentPayload createStringSelectMenuPayload(SelectMenuConfigModel selectMenuConfigModel, Long serverId) {
        AServer server = serverManagementService.loadOrCreate(serverId);
        return createStringSelectMenuPayload(selectMenuConfigModel, server);
    }

    @Override
    public ComponentPayload createStringSelectMenuPayload(SelectMenuConfigModel selectMenuConfigModel, AServer server) {
        String payload = gson.toJson(selectMenuConfigModel.getSelectMenuPayload());
        return createPayload(selectMenuConfigModel.getSelectMenuId(), payload, selectMenuConfigModel.getPayloadType(), selectMenuConfigModel.getOrigin(), server, ComponentType.SELECTION);
    }

    @Override
    public ComponentPayload createModalPayload(ModalConfigPayload payloadConfig, Long serverId) {
        String payload = gson.toJson(payloadConfig.getModalPayload());
        AServer server = serverManagementService.loadOrCreate(serverId);
        return createPayload(payloadConfig.getModalId(), payload, payloadConfig.getPayloadType(), payloadConfig.getOrigin(), server, ComponentType.MODAL);
    }

    @Override
    public Optional<ComponentPayload> findPayload(String id) {
        return repository.findById(id);
    }

    @Override
    public List<ComponentPayload> findPayloadsOfOriginInServer(String buttonOrigin, AServer server) {
        return repository.findByServerAndOrigin(server, buttonOrigin);
    }

    @Override
    public void deletePayload(String id) {
        repository.deleteById(id);
    }

    @Override
    public void deletePayloads(List<String> ids) {
        ids.forEach(payloadId -> log.info("Deleting payload {}", payloadId));
        repository.deleteByIdIn(ids);
    }

    @Override
    public void deletePayload(ComponentPayload payload) {
        repository.delete(payload);
    }
}
