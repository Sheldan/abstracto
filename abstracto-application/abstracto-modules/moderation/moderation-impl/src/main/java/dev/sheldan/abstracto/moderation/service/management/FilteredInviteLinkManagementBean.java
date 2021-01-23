package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.FilteredInviteLink;
import dev.sheldan.abstracto.moderation.repository.FilteredInviteLinkRepository;
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
    public FilteredInviteLink createFilteredInviteLink(AServer server, String code) {
        FilteredInviteLink inviteLink = FilteredInviteLink
                .builder()
                .code(code)
                .server(server)
                .uses(1L)
                .build();
        return repository.save(inviteLink);
    }

    @Override
    public Optional<FilteredInviteLink> findInviteLinkViaCode(AServer server, String code) {
        return repository.findByCodeAndServer(code, server);
    }

    @Override
    public Optional<FilteredInviteLink> findInviteLinkViaCode(Long serverId, String code) {
        return repository.findByCodeAndServer_Id(code, serverId);
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
    public void clearFilteredInviteLink(String code, Long serverId) {
        repository.deleteByCodeAndServer_Id(code, serverId);
    }

    @Override
    public void clearFilteredInviteLink(String code, AServer server) {
        clearFilteredInviteLink(code, server.getId());
    }

    @Override
    public List<FilteredInviteLink> getTopFilteredInviteLink(Long serverId, Integer count) {
        return repository.findAllByServer_IdOrderByUsesDesc(serverId,  PageRequest.of(0, count));
    }
}
