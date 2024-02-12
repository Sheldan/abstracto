package dev.sheldan.abstracto.stickyroles.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.stickyroles.model.database.StickyRoleUser;
import net.dv8tion.jda.api.entities.Member;

public interface StickyRoleUserManagementService {
    default StickyRoleUser getOrCreateStickyRoleUser(Member member) {
        return getOrCreateStickyRoleUser(member.getGuild().getIdLong(), member.getIdLong());
    }
    StickyRoleUser getOrCreateStickyRoleUser(Long serverId, Long userId);
    StickyRoleUser createStickyroleUser(Long serverId, Long userId);
    StickyRoleUser createStickyroleUser(AUserInAServer userInAServer);
}
