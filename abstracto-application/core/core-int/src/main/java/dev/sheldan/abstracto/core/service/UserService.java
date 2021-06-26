package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<User> retrieveUserForId(Long id);
    CompletableFutureList<User> retrieveUsers(List<Long> ids);
    SelfUser getSelfUser();
}
