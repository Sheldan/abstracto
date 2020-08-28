package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserInServerServiceBean implements UserInServerService {

    @Autowired
    private BotService botService;

    @Override
    public FullUserInServer getFullUser(AUserInAServer aUserInAServer) {
        return FullUserInServer
                .builder()
                .member(botService.getMemberInServer(aUserInAServer))
                .aUserInAServer(aUserInAServer)
                .build();
    }
}
