package dev.sheldan.abstracto.core.converter;

import dev.sheldan.abstracto.core.models.dto.UserDto;
import dev.sheldan.abstracto.core.models.template.UserModel;
import org.springframework.stereotype.Component;

@Component
public class UserModelConverter {
    public UserModel fromUser(UserDto user) {
        return UserModel.builder().id(user.getId()).build();
    }
}
