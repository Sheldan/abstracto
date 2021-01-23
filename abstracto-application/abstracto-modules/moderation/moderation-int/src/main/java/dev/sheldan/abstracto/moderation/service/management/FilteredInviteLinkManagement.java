package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.FilteredInviteLink;

import java.util.List;
import java.util.Optional;

public interface FilteredInviteLinkManagement {
    FilteredInviteLink createFilteredInviteLink(AServer server, String code);
    Optional<FilteredInviteLink> findInviteLinkViaCode(AServer server, String code);
    Optional<FilteredInviteLink> findInviteLinkViaCode(Long serverId, String code);
    void clearFilteredInviteLinks(Long serverId);
    void clearFilteredInviteLinks(AServer server);
    void clearFilteredInviteLink(String code, Long serverId);
    void clearFilteredInviteLink(String code, AServer server);
    List<FilteredInviteLink> getTopFilteredInviteLink(Long serverId, Integer count);
}
