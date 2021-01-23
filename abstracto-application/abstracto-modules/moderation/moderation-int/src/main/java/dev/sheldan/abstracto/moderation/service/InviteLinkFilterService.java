package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.moderation.models.database.FilteredInviteLink;

import java.util.List;

public interface InviteLinkFilterService {
    boolean isCodeFiltered(String code, ServerUser serverUser);
    boolean isCodeAllowed(String code, ServerUser serverUser);
    boolean isCodeAllowed(String code, Long serverId);
    void storeFilteredInviteLinkUsage(String code, ServerUser serverUser);
    void allowInvite(String invite, Long serverId);
    void disAllowInvite(String invite, Long serverId);
    void clearAllTrackedInviteCodes(Long serverId);
    void clearAllUses(String code, Long serverId);
    List<FilteredInviteLink> getTopFilteredInviteLinks(Long serverId, Integer count);
    List<FilteredInviteLink> getTopFilteredInviteLinks(Long serverId);
}
