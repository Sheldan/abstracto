package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.command.service.UserService;
import dev.sheldan.abstracto.core.models.converter.UserInServerConverter;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.core.service.management.UserManagementServiceBean;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserServiceBean implements UserService {

    @Autowired
    private UserManagementServiceBean userManagementServiceBean;

    @Override
    public UserInServerDto loadUser(Member member) {
        return userManagementServiceBean.loadUser(member.getGuild().getIdLong(), member.getIdLong());
    }

    @Override
    public UserInServerDto loadUser(Long serverId, Long userId) {
        return userManagementServiceBean.loadUser(serverId, userId);
    }
}
