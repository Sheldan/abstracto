package dev.sheldan.abstracto.stickyroles.service.management;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.stickyroles.model.database.StickyRoleUser;
import dev.sheldan.abstracto.stickyroles.repository.StickyRoleUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StickyRoleUserManagementServiceBean implements StickyRoleUserManagementService {

    @Autowired
    private StickyRoleUserRepository repository;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public StickyRoleUser getOrCreateStickyRoleUser(Long serverId, Long userId) {
        ServerUser serverUser = ServerUser
                .builder()
                .userId(userId)
                .serverId(serverId)
                .build();
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(serverUser);
        return repository.findById(userInAServer.getUserInServerId()).orElseGet(() -> createStickyroleUser(userInAServer));
    }

    @Override
    public StickyRoleUser createStickyroleUser(Long serverId, Long userId) {
        ServerUser serverUser = ServerUser
                .builder()
                .userId(userId)
                .serverId(serverId)
                .build();
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(serverUser);
        return createStickyroleUser(userInAServer);
    }

    @Override
    public StickyRoleUser createStickyroleUser(AUserInAServer userInAServer) {
        StickyRoleUser stickyRoleUser = StickyRoleUser
                .builder()
                .user(userInAServer)
                .server(userInAServer.getServerReference())
                .id(userInAServer.getUserInServerId())
                .sticky(true)
                .build();
        return repository.save(stickyRoleUser);
    }
}
