package dev.sheldan.abstracto.core.converter;

import dev.sheldan.abstracto.core.command.service.UserService;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.core.models.template.UserInServerModel;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserInServerModelConverter {

    @Autowired
    private ServerModelConverter serverModelConverter;

    @Autowired
    private UserModelConverter userModelConverter;

    @Autowired
    private UserService userService;


    public UserInServerModel fromUser(UserInServerDto userInServerDto) {
        return UserInServerModel
                .builder()
                .userInServerId(userInServerDto.getUserInServerId())
                .server(serverModelConverter.fromServer(userInServerDto.getServer()))
                .user(userModelConverter.fromUser(userInServerDto.getUser()))
                .build();
    }

    public UserInServerModel fromMember(Member member) {
        return fromUser(userService.loadUser(member));
    }


}
