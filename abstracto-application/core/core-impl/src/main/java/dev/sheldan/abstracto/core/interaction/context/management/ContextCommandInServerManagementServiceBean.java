package dev.sheldan.abstracto.core.interaction.context.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ContextCommand;
import dev.sheldan.abstracto.core.models.database.ContextCommandInServer;
import dev.sheldan.abstracto.core.repository.ContextCommandInServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ContextCommandInServerManagementServiceBean implements ContextCommandInServerManagementService {

    @Autowired
    private ContextCommandInServerRepository contextCommandInServerRepository;

    @Override
    public ContextCommandInServer createOrUpdateContextCommandInServer(ContextCommand contextCommand, AServer server, Long discordContextId) {
        Optional<ContextCommandInServer> optional = loadContextCommandInServer(contextCommand, server);
        if(optional.isPresent()) {
            ContextCommandInServer contextCommandInServer = optional.get();
            contextCommandInServer.setContextCommandId(discordContextId);
            return contextCommandInServer;
        } else {
            ContextCommandInServer contextCommandInServer = ContextCommandInServer
                    .builder()
                    .commandReference(contextCommand)
                    .serverReference(server)
                    .contextCommandId(discordContextId)
                    .build();
            return contextCommandInServerRepository.save(contextCommandInServer);
        }
    }

    @Override
    public Optional<ContextCommandInServer> loadContextCommandInServer(ContextCommand contextCommand, AServer server) {
        return contextCommandInServerRepository.findByCommandReferenceAndServerReference(contextCommand, server);
    }
}
