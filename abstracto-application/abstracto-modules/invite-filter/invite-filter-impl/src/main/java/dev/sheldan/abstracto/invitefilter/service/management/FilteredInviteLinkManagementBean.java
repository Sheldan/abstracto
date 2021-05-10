package dev.sheldan.abstracto.invitefilter.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.invitefilter.model.database.FilteredInviteLink;
import dev.sheldan.abstracto.invitefilter.repository.FilteredInviteLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class FilteredInviteLinkManagementBean implements FilteredInviteLinkManagement {

    @Autowired
    private FilteredInviteLinkRepository repository;

    @Override
    public FilteredInviteLink createFilteredInviteLink(AServer server, Long targetServerId, String serverName) {
        FilteredInviteLink inviteLink = FilteredInviteLink
                .builder()
                .server(server)
                .serverName(serverName)
                .targetServerId(targetServerId)
                .uses(1L)
                .build();
        return repository.save(inviteLink);
    }

    @Override
    public Optional<FilteredInviteLink> findInviteLinkViaTargetID(AServer server, Long targetServerId) {
        return repository.findByTargetServerIdAndServer(targetServerId, server);
    }

    @Override
    public Optional<FilteredInviteLink> findInviteLinkViaTargetID(Long serverId, Long targetServerId) {
        return repository.findByTargetServerIdAndServer_Id(targetServerId, serverId);
    }

    @Override
    public void clearFilteredInviteLinks(Long serverId) {
        repository.deleteByServer_Id(serverId);
    }

    @Override
    public void clearFilteredInviteLinks(AServer server) {
        clearFilteredInviteLinks(server.getId());
    }

    @Override
    public void clearFilteredInviteLink(Long targetServerId, Long serverId) {
        repository.deleteByTargetServerIdAndServer_Id(targetServerId, serverId);
    }

    @Override
    public void clearFilteredInviteLink(Long targetServerId, AServer server) {
        clearFilteredInviteLink(targetServerId, server.getId());
    }

    @Override
    public List<FilteredInviteLink> getTopFilteredInviteLink(Long serverId, Integer count) {
        return repository.findAllByServer_IdOrderByUsesDesc(serverId,  PageRequest.of(0, count));
    }
}
