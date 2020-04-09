package dev.sheldan.abstracto.core.models.converter;

import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.AUser;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.models.dto.UserDto;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserInServerConverter {

    @Autowired
    private ServerConverter serverConverter;

    @Autowired
    private UserConverter userConverter;

    public UserInServerDto fromAUserInAServer(AUserInAServer userInAServer) {
        ServerDto server = ServerDto.builder().id(userInAServer.getServerReference().getId()).build();
        UserDto user = userConverter.fromAUser(userInAServer.getUserReference());
        return UserInServerDto
                .builder()
                .server(server)
                .user(user)
                .userInServerId(userInAServer.getUserInServerId())
                .build();
    }

    public  AUserInAServer fromDto(UserInServerDto userInServerDto) {
        AUser user = userConverter.toUser(userInServerDto.getUser());
        AServer server = AServer.builder().id(userInServerDto.getServer().getId()).build();
        return AUserInAServer
                .builder()
                .userInServerId(userInServerDto.getUserInServerId())
                .serverReference(server)
                .userReference(user)
                .build();
    }
}
