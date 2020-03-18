package dev.sheldan.abstracto.core.management;

import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.AUser;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import net.dv8tion.jda.api.entities.Member;

public interface UserManagementService {
    AUserInAServer loadUser(Long userId, Long serverId);
    AUserInAServer loadUser(AUser user, AServer server);
    AUserInAServer loadUser(Member member);
    AUserInAServer createUserInServer(Member member);
    AUser createUser(Member member);
}
