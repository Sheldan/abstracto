package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.*;
import dev.sheldan.abstracto.core.models.converter.UserConverter;
import dev.sheldan.abstracto.core.models.converter.UserInServerConverter;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.models.dto.UserDto;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.core.repository.UserInServerRepository;
import dev.sheldan.abstracto.core.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserManagementServiceBean {

    @Autowired
    private UserInServerRepository userInServerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerManagementServiceBean serverManagementService;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private UserInServerConverter userInServerConverter;


    public UserInServerDto loadUser(Long serverId, Long userId) {
        UserDto user = UserDto.builder().id(userId).build();
        ServerDto serverDto = ServerDto.builder().id(userId).build();
        return loadUser(serverDto, user);
    }

    public UserInServerDto loadUser(ServerDto server, UserDto user) {
        AServer server1 = AServer.builder().id(server.getId()).build();
        AUser user1 = AUser.builder().id(user.getId()).build();
        if(userInServerRepository.existsByServerReferenceAndUserReference(server1, user1)) {
            AUserInAServer byServerReferenceAndUserReference = userInServerRepository.findByServerReferenceAndUserReference(server1, user1);
            return userInServerConverter.fromAUserInAServer(byServerReferenceAndUserReference);
        } else {
            return this.createUserInServer(server.getId(), user.getId());
        }
    }

    public UserInServerDto loadUser(Member member) {
        return this.loadUser(member.getGuild().getIdLong(), member.getIdLong());
    }

    public UserInServerDto createUserInServer(Member member) {
        return this.createUserInServer(member.getGuild().getIdLong(), member.getIdLong());
    }

    public UserInServerDto createUserInServer(Long guildId, Long userId) {
        return serverManagementService.addUserToServer(guildId, userId);
    }

    public UserDto createUser(Member member) {
       return createUser(member.getIdLong());
    }

    public UserDto createUser(Long userId) {
        log.info("Creating user {}", userId);
        AUser aUser = AUser.builder().id(userId).build();
        userRepository.save(aUser);
        return userConverter.fromAUser(aUser);
    }

    public UserDto loadUser(Long userId) {
        if(userRepository.existsById(userId)) {
            return userConverter.fromAUser(userRepository.getOne(userId));
        } else {
            return this.createUser(userId);
        }
    }
}
