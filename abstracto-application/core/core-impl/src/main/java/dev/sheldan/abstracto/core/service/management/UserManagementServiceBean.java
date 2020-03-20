package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.management.UserManagementService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.repository.UserInServerRepository;
import dev.sheldan.abstracto.repository.UserRepository;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserManagementServiceBean implements UserManagementService {

    @Autowired
    private UserInServerRepository userInServerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerManagementService serverManagementService;


    @Override
    public AUserInAServer loadUser(Long userId, Long serverId) {
        AUser user = userRepository.getOne(userId);
        AServer server = serverManagementService.loadServer(serverId);
        return loadUser(user, server);
    }

    @Override
    public AUserInAServer loadUser(AUser user, AServer server) {
        return userInServerRepository.findByServerReferenceAndUserReference(server, user);
    }

    @Override
    public AUserInAServer loadUser(Member member) {
        AUserInAServer aUserInAServer = this.loadUser(member.getGuild().getIdLong(), member.getIdLong());
        if(aUserInAServer == null) {
            return this.createUserInServer(member);
        }
        return null;
    }

    @Override
    public AUserInAServer createUserInServer(Member member) {
        AServer server = serverManagementService.loadServer(member.getGuild().getIdLong());

        if(!userRepository.existsById(member.getIdLong())) {
            this.createUser(member);
        }
        AUser aUser = userRepository.getOne(member.getIdLong());
        return serverManagementService.addUserToServer(server, aUser);
    }

    @Override
    public AUser createUser(Member member) {
        AUser aUser = AUser.builder().id(member.getIdLong()).build();
        userRepository.save(aUser);
        return aUser;
    }
}
