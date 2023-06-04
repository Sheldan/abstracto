package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.*;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;
import java.util.Optional;

public interface ServerManagementService {
    AServer createServer(Long id);
    AServer loadOrCreate(Long id);
    AServer loadServer(Long id);
    AServer loadServer(Guild guild);
    Optional<AServer> loadServerOptional(Long id);
    void addChannelToServer(AServer server, AChannel channel);
    AUserInAServer addUserToServer(AServer server, AUser user);
    AUserInAServer addUserToServer(Long serverId, Long userId);
    List<AServer> getAllServers();
}
