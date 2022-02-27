package dev.sheldan.abstracto.core.service;


import net.dv8tion.jda.api.entities.GuildMessageChannel;

import java.util.concurrent.CompletableFuture;

public interface PaginatorService {
    CompletableFuture<Void> createPaginatorFromTemplate(String templateKey, Object model, GuildMessageChannel textChannel, Long userId);
}
