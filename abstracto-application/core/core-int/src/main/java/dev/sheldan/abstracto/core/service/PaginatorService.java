package dev.sheldan.abstracto.core.service;


import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.CompletableFuture;

public interface PaginatorService {
    CompletableFuture<Void> createPaginatorFromTemplate(String templateKey, Object model, TextChannel textChannel, Long userId);
}
