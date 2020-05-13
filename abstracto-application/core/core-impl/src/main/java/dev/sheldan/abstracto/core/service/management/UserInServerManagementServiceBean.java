package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.repository.UserInServerRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class UserInServerManagementServiceBean implements UserInServerManagementService {

    @Autowired
    private UserInServerRepository userInServerRepository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserManagementService userManagementService;


    @Override
    public AUserInAServer loadUser(Long serverId, Long userId) {
        AUser user = userManagementService.loadUser(userId);
        AServer server = serverManagementService.loadOrCreate(serverId);
        return loadUser(server, user);
    }

    @Override
    public AUserInAServer loadUser(AServer server, AUser user) {
        if(userInServerRepository.existsByServerReferenceAndUserReference(server, user)) {
            return userInServerRepository.findByServerReferenceAndUserReference(server, user);
        } else {
            return this.createUserInServer(server.getId(), user.getId());
        }
    }

    @Override
    public AUserInAServer loadUser(Member member) {
        return this.loadUser(member.getGuild().getIdLong(), member.getIdLong());
    }

    @Override
    public Optional<AUserInAServer> loadUser(Long userInServerId) {
        return userInServerRepository.findById(userInServerId);
    }

    @Override
    public AUserInAServer createUserInServer(Member member) {
        return this.createUserInServer(member.getGuild().getIdLong(), member.getIdLong());
    }

    @Override
    public AUserInAServer createUserInServer(Long guildId, Long userId) {
        AUserInAServer aUserInAServer = serverManagementService.addUserToServer(guildId, userId);
        userInServerRepository.save(aUserInAServer);
        return aUserInAServer;
    }

    @Override
    public List<AUserInAServer> getUserInAllServers(Long userId) {
        AUser user = userManagementService.loadUser(userId);
        return userInServerRepository.findByUserReference(user);
    }
}
