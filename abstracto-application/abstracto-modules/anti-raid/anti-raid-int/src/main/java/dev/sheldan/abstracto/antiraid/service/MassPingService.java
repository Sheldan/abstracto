package dev.sheldan.abstracto.antiraid.service;

import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

public interface MassPingService {
    String MAX_AFFECTED_LEVEL_KEY = "massPingMinLevel";
    CompletableFuture<Void> processMessage(Message message);
}
