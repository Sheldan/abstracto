package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.User;

import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<User> retrieveUserForId(Long id);
}
