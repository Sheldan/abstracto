package dev.sheldan.abstracto.invitefilter.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.invitefilter.model.database.FilteredInviteLink;

import java.util.List;
import java.util.Optional;

public interface FilteredInviteLinkManagement {
    FilteredInviteLink createFilteredInviteLink(AServer server, Long targetServerId, String serverName);
    Optional<FilteredInviteLink> findInviteLinkViaTargetID(AServer server, Long targetServerId);
    Optional<FilteredInviteLink> findInviteLinkViaTargetID(Long serverId, Long targetServerId);
    void clearFilteredInviteLinks(Long serverId);
    void clearFilteredInviteLinks(AServer server);
    void clearFilteredInviteLink(Long targetServerId, Long serverId);
    void clearFilteredInviteLink(Long targetServerId, AServer server);
    List<FilteredInviteLink> getTopFilteredInviteLink(Long serverId, Integer count);
}
