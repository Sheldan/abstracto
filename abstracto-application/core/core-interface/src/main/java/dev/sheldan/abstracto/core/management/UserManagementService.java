package dev.sheldan.abstracto.core.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;

public interface UserManagementService {
    AUserInAServer loadUser(Long serverId, Long userId);
    AUserInAServer loadUser(AServer server, AUser user);
    AUserInAServer loadUser(Member member);
    AUserInAServer createUserInServer(Member member);
    AUserInAServer createUserInServer(Long guildId, Long userId);
    AUser createUser(Member member);
    AUser createUser(Long userId);
    AUser loadUser(Long userId);
}
