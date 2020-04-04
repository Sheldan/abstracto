package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.repository.UserInServerRepository;
import dev.sheldan.abstracto.core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserManagementServiceBean implements UserManagementService {

    @Autowired
    private UserInServerRepository userInServerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerManagementService serverManagementService;


    @Override
    public AUserInAServer loadUser(Long serverId, Long userId) {
        AUser user = this.loadUser(userId);
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
    public AUserInAServer createUserInServer(Member member) {
        return this.createUserInServer(member.getGuild().getIdLong(), member.getIdLong());
    }

    @Override
    public AUserInAServer createUserInServer(Long guildId, Long userId) {
        return serverManagementService.addUserToServer(guildId, userId);
    }

    @Override
    public AUser createUser(Member member) {
       return createUser(member.getIdLong());
    }

    @Override
    public AUser createUser(Long userId) {
        log.info("Creating user {}", userId);
        AUser aUser = AUser.builder().id(userId).build();
        userRepository.save(aUser);
        return aUser;
    }

    @Override
    public AUser loadUser(Long userId) {
        if(userRepository.existsById(userId)) {
            return userRepository.getOne(userId);
        } else {
            return this.createUser(userId);
        }
    }
}
