package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.database.AChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public interface SlowModeService {
    CompletableFuture<Void> setSlowMode(TextChannel channel, Duration duration);
    CompletableFuture<Void> disableSlowMode(TextChannel channel);
    CompletableFuture<Void> setSlowMode(AChannel channel, Duration duration);
}
