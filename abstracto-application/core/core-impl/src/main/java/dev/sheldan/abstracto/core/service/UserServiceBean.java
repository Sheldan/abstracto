package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UserServiceBean implements UserService {

    @Autowired
    private BotService botService;

    @Override
    public CompletableFuture<User> retrieveUserForId(Long id) {
        return botService.getInstance().retrieveUserById(id).submit();
    }
}
