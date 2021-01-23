package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.moderation.models.database.AllowedInviteLink;

public interface AllowedInviteLinkManagement {
    AllowedInviteLink createAllowedInviteLink(AServer server, String code);
    void removeAllowedInviteLink(AServer server, String code);
    AllowedInviteLink findAllowedInviteLinkByCode(AServer server, String code);
    boolean allowedInviteLinkExists(AServer server, String code);
    boolean allowedInviteLinkExists(Long serverId, String code);
    boolean allowedInviteLinkExists(ServerUser serverUser, String code);
}
