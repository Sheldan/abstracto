package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.exception.AllowedInviteLinkNotFound;
import dev.sheldan.abstracto.moderation.models.database.AllowedInviteLink;
import dev.sheldan.abstracto.moderation.repository.AllowedInviteLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AllowedInviteLinkManagementBean implements AllowedInviteLinkManagement {

    @Autowired
    private AllowedInviteLinkRepository repository;

    @Override
    public AllowedInviteLink createAllowedInviteLink(AServer server, String code) {
        AllowedInviteLink inviteLink = AllowedInviteLink.builder().code(code).server(server).build();
        return repository.save(inviteLink);
    }

    @Override
    public void removeAllowedInviteLink(AServer server, String code) {
        AllowedInviteLink existingCode = findAllowedInviteLinkByCode(server, code);
        repository.delete(existingCode);
    }

    @Override
    public AllowedInviteLink findAllowedInviteLinkByCode(AServer server, String code) {
        return repository.findByCodeAndServer(code, server).orElseThrow(() -> new AllowedInviteLinkNotFound("Allowed invite code not found."));
    }

    @Override
    public boolean allowedInviteLinkExists(AServer server, String code) {
        return repository.findByCodeAndServer(code, server).isPresent();
    }

    @Override
    public boolean allowedInviteLinkExists(Long serverId, String code) {
        return repository.findByCodeAndServer_Id(code, serverId).isPresent();
    }

    @Override
    public boolean allowedInviteLinkExists(ServerUser serverUser, String code) {
        return repository.findByCodeAndServer_Id(code, serverUser.getServerId()).isPresent();
    }
}
