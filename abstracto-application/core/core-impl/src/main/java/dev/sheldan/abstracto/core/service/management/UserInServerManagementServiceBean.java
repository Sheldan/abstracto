package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.command.models.TableLocks;
import dev.sheldan.abstracto.core.exception.UserInServerNotFoundException;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.repository.UserInServerRepository;
import dev.sheldan.abstracto.core.service.LockService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private LockService lockService;

    @Autowired
    private UserInServerManagementServiceBean self;

    @Override
    public AUserInAServer loadOrCreateUser(Long serverId, Long userId) {
        if(userInServerRepository.existsByServerReference_IdAndUserReference_Id(serverId, userId)) {
            return userInServerRepository.findByServerReference_IdAndUserReference_Id(serverId, userId).orElseThrow(() -> new UserInServerNotFoundException(0L));
        } else {
            return this.createUserInServer(serverId, userId);
        }
    }

    @Override
    public AUserInAServer onlyLoadUser(Long serverId, Long userId) {
        return userInServerRepository.findByServerReference_IdAndUserReference_Id(serverId, userId).orElseThrow(() -> new UserInServerNotFoundException(0L));
    }

    @Override
    public AUserInAServer loadOrCreateUser(ServerUser serverUser) {
        return loadOrCreateUser(serverUser.getServerId(), serverUser.getUserId());
    }

    @Override
    public Optional<AUserInAServer> loadUserOptional(Long serverId, Long userId) {
        return userInServerRepository.findByServerReference_IdAndUserReference_Id(serverId, userId);
    }

    @Override
    public AUserInAServer loadOrCreateUser(AServer server, AUser user) {
        if(userInServerRepository.existsByServerReferenceAndUserReference(server, user)) {
            return userInServerRepository.findByServerReferenceAndUserReference(server, user).orElseThrow(() -> new UserInServerNotFoundException(0L));
        } else {
            return this.createUserInServer(server.getId(), user.getId());
        }
    }

    @Override
    public AUserInAServer loadOrCreateUser(Member member) {
        return this.loadOrCreateUser(member.getGuild().getIdLong(), member.getIdLong());
    }

    @Override
    public Optional<AUserInAServer> loadUserOptional(Long userInServerId) {
        return userInServerRepository.findById(userInServerId);
    }

    @Override
    public AUserInAServer loadOrCreateUser(Long userInServerId) {
        return loadUserOptional(userInServerId).orElseThrow(() -> new UserInServerNotFoundException(userInServerId));
    }

    @Override
    public AUserInAServer createUserInServer(Member member) {
        return this.createUserInServer(member.getGuild().getIdLong(), member.getIdLong());
    }

    @Override
    public AUserInAServer createUserInServer(Long serverId, Long userId) {
        log.info("Creating user {} in server {}.", userId, serverId);
        AUserInAServer aUserInAServer;
        try {
            aUserInAServer = self.tryToCreateAUserInAServer(serverId, userId);
        } catch (DataIntegrityViolationException ex) {
            log.info("Concurrency exception creating user - retrieving.");
            aUserInAServer = onlyLoadUser(serverId, userId);
        }
        return aUserInAServer;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AUserInAServer tryToCreateAUserInAServer(Long guildId, Long userId) {
        lockService.lockTable(TableLocks.USER_IN_SERVER);
        AUserInAServer aUserInAServer = serverManagementService.addUserToServer(guildId, userId);
        userInServerRepository.save(aUserInAServer);
        return aUserInAServer;
    }

    @Override
    public List<AUserInAServer> getUserInAllServers(Long userId) {
        AUser user = userManagementService.loadOrCreateUser(userId);
        return userInServerRepository.findByUserReference(user);
    }

    @Override
    public Optional<AUserInAServer> loadAUserInAServerOptional(Long serverId, Long userId) {
        AUser user = userManagementService.loadOrCreateUser(userId);
        AServer server = serverManagementService.loadServer(serverId);
        return userInServerRepository.findByServerReferenceAndUserReference(server, user);
    }
}
