package dev.sheldan.abstracto.customcommand.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import dev.sheldan.abstracto.customcommand.repository.CustomCommandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomCommandManagementServiceBean implements CustomCommandManagementService {

    @Autowired
    private CustomCommandRepository repository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public Optional<CustomCommand> getCustomCommandByName(String name, Long serverId) {
        AServer server = serverManagementService.loadServer(serverId);
        return repository.getByNameAndServer(name, server);
    }
}
