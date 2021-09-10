package dev.sheldan.abstracto.core.service.management;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.models.database.ComponentType;
import dev.sheldan.abstracto.core.models.template.button.ButtonConfigModel;
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

    @Override
    public ComponentPayload createPayload(String id, String payload, Class payloadType, String buttonOrigin, AServer server, ComponentType componentType) {
        ComponentPayload componentPayload = ComponentPayload
                .builder()
                .origin(buttonOrigin)
                .id(id)
                .payload(payload)
                .payloadType(payloadType.getTypeName())
                .server(server)
                .type(componentType)
                .build();
        return repository.save(componentPayload);
    }

    @Override
    public ComponentPayload createPayload(ButtonConfigModel buttonConfigModel, AServer server) {
        String payload = gson.toJson(buttonConfigModel.getButtonPayload());
        return createPayload(buttonConfigModel.getButtonId(), payload, buttonConfigModel.getPayloadType(), buttonConfigModel.getOrigin(), server, ComponentType.BUTTON);
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
