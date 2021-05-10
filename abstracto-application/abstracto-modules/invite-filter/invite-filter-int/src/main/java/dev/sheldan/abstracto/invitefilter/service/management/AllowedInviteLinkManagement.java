package dev.sheldan.abstracto.invitefilter.service.management;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.invitefilter.model.database.AllowedInviteLink;

public interface AllowedInviteLinkManagement {
    AllowedInviteLink createAllowedInviteLink(AServer server, Long targetServerId, String code);
    void removeAllowedInviteLink(AServer server, Long targetServerId);
    AllowedInviteLink findAllowedInviteLinkByCode(AServer server, Long targetServerId);
    boolean allowedInviteLinkExists(AServer server, Long targetServerId);
    boolean allowedInviteLinkExists(Long serverId, Long targetServerId);
    boolean allowedInviteLinkExists(ServerUser serverUser, Long targetServerId);
    boolean allowedInviteLinkExists(ServerUser serverUser, String code);
}
