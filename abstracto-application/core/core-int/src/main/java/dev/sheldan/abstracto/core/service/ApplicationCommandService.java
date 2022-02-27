package dev.sheldan.abstracto.core.service;

import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.CompletableFuture;

public interface ApplicationCommandService {
    CompletableFuture<Void> deleteGuildCommand(Guild guild, Long commandId);
}
