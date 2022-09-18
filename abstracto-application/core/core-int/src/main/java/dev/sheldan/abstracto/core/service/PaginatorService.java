package dev.sheldan.abstracto.core.service;


import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.concurrent.CompletableFuture;

public interface PaginatorService {
    CompletableFuture<Void> createPaginatorFromTemplate(String templateKey, Object model, GuildMessageChannel textChannel, Long userId);
    CompletableFuture<Void> createPaginatorFromTemplate(String templateKey, Object model, IReplyCallback callback);
}
