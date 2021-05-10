package dev.sheldan.abstracto.invitefilter.service.management;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.invitefilter.exception.AllowedInviteLinkNotFound;
import dev.sheldan.abstracto.invitefilter.model.database.AllowedInviteLink;
import dev.sheldan.abstracto.invitefilter.repository.AllowedInviteLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AllowedInviteLinkManagementBean implements AllowedInviteLinkManagement {

    @Autowired
    private AllowedInviteLinkRepository repository;

    @Override
    public AllowedInviteLink createAllowedInviteLink(AServer server, Long targetServerId, String code) {
        AllowedInviteLink inviteLink = AllowedInviteLink.builder().targetServerId(targetServerId).code(code).server(server).build();
        return repository.save(inviteLink);
    }

    @Override
    public void removeAllowedInviteLink(AServer server, Long targetServerId) {
        AllowedInviteLink existingCode = findAllowedInviteLinkByCode(server, targetServerId);
        repository.delete(existingCode);
    }

    @Override
    public AllowedInviteLink findAllowedInviteLinkByCode(AServer server, Long targetServerId) {
        return repository.findByTargetServerIdAndServer(targetServerId, server).orElseThrow(() -> new AllowedInviteLinkNotFound("Allowed invite code not found."));
    }

    @Override
    public boolean allowedInviteLinkExists(AServer server, Long targetServerId) {
        return repository.findByTargetServerIdAndServer(targetServerId, server).isPresent();
    }

    @Override
    public boolean allowedInviteLinkExists(Long serverId, Long targetServerId) {
        return repository.findByTargetServerIdAndServer_Id(targetServerId, serverId).isPresent();
    }

    @Override
    public boolean allowedInviteLinkExists(ServerUser serverUser, Long targetServerId) {
        return repository.findByTargetServerIdAndServer_Id(targetServerId, serverUser.getServerId()).isPresent();
    }

    @Override
    public boolean allowedInviteLinkExists(ServerUser serverUser, String code) {
        return repository.findByCodeAndServer_Id(code, serverUser.getServerId()).isPresent();
    }
}
