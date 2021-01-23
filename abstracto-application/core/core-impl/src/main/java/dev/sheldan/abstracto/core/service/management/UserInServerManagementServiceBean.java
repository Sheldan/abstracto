package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.exception.UserInServerNotFoundException;
import dev.sheldan.abstracto.core.models.ServerUser;
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
        if(userInServerRepository.existsByServerReference_IdAndUserReference_Id(serverId, userId)) {
            return userInServerRepository.findByServerReference_IdAndUserReference_Id(serverId, userId).orElseThrow(() -> new UserInServerNotFoundException(0L));
        } else {
            return this.createUserInServer(serverId, userId);
        }
    }

    @Override
    public AUserInAServer loadUser(ServerUser serverUser) {
        return loadUser(serverUser.getServerId(), serverUser.getUserId());
    }

    @Override
    public Optional<AUserInAServer> loadUserOptional(Long serverId, Long userId) {
        return userInServerRepository.findByServerReference_IdAndUserReference_Id(serverId, userId);
    }

    @Override
    public AUserInAServer loadUser(AServer server, AUser user) {
        if(userInServerRepository.existsByServerReferenceAndUserReference(server, user)) {
            return userInServerRepository.findByServerReferenceAndUserReference(server, user).orElseThrow(() -> new UserInServerNotFoundException(0L));
        } else {
            return this.createUserInServer(server.getId(), user.getId());
        }
    }

    @Override
    public AUserInAServer loadUser(Member member) {
        return this.loadUser(member.getGuild().getIdLong(), member.getIdLong());
    }

    @Override
    public Optional<AUserInAServer> loadUserOptional(Long userInServerId) {
        return userInServerRepository.findById(userInServerId);
    }

    @Override
    public AUserInAServer loadUser(Long userInServerId) {
        return loadUserOptional(userInServerId).orElseThrow(() -> new UserInServerNotFoundException(userInServerId));
    }

    @Override
    public AUserInAServer createUserInServer(Member member) {
        return this.createUserInServer(member.getGuild().getIdLong(), member.getIdLong());
    }

    @Override
    public AUserInAServer createUserInServer(Long guildId, Long userId) {
        log.info("Creating user {} in server {}.", userId, guildId);
        AUserInAServer aUserInAServer = serverManagementService.addUserToServer(guildId, userId);
        userInServerRepository.save(aUserInAServer);
        return aUserInAServer;
    }

    @Override
    public List<AUserInAServer> getUserInAllServers(Long userId) {
        AUser user = userManagementService.loadUser(userId);
        return userInServerRepository.findByUserReference(user);
    }

    @Override
    public Optional<AUserInAServer> loadAUserInAServerOptional(Long serverId, Long userId) {
        AUser user = userManagementService.loadUser(userId);
        AServer server = serverManagementService.loadServer(serverId);
        return userInServerRepository.findByServerReferenceAndUserReference(server, user);
    }
}
