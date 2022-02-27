package dev.sheldan.abstracto.moderation.service;

import net.dv8tion.jda.api.entities.*;

import java.util.concurrent.CompletableFuture;

public interface PurgeService {
    CompletableFuture<Void> purgeMessagesInChannel(Integer count, GuildMessageChannel channel, Long messageId, Member purgingRestriction);
    CompletableFuture<Void> purgeMessagesInChannel(Integer count, GuildMessageChannel channel, Message origin, Member purgingRestriction);
}
