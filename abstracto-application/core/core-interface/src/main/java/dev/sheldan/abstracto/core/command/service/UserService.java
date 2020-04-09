package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import net.dv8tion.jda.api.entities.Member;

public interface UserService {
    UserInServerDto loadUser(Member member);
    UserInServerDto loadUser(Long serverId,  Long userId);
}
