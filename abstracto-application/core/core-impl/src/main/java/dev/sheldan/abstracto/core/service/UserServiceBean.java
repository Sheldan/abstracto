package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class UserServiceBean implements UserService {

    @Autowired
    private BotService botService;

    @Override
    public CompletableFuture<User> retrieveUserForId(Long id) {
        return botService.getInstance().retrieveUserById(id).submit();
    }

    @Override
    public CompletableFutureList<User> retrieveUsers(List<Long> ids) {
        List<CompletableFuture<User>> userFutures = ids
                .stream()
                .map(this::retrieveUserForId)
                .collect(Collectors.toList());
        return new CompletableFutureList<>(userFutures);
    }

    @Override
    public SelfUser getSelfUser() {
        return botService.getInstance().getSelfUser();
    }
}
