package dev.sheldan.abstracto.core.models.converter;

import dev.sheldan.abstracto.core.models.AUser;
import dev.sheldan.abstracto.core.models.dto.UserDto;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public UserDto fromAUser(AUser user) {
        return UserDto.builder().id(user.getId()).build();
    }

    public UserDto fromMember(Member member) {
        return UserDto.builder().id(member.getIdLong()).build();
    }

    public AUser toUser(UserDto userDto) {
        return AUser.builder().id(userDto.getId()).build();
    }
}
