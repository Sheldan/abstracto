package dev.sheldan.abstracto.moderation.service;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface PurgeService {
    CompletableFuture<Void> purgeMessagesInChannel(Integer count, GuildMessageChannel channel, Long messageId, Member purgingRestriction);
    CompletableFuture<Void> purgeMessagesInChannel(Integer count, GuildMessageChannel channel, Message origin, Member purgingRestriction);
    CompletionStage<Void> purgeMessagesInChannel(Integer amountOfMessages, GuildMessageChannel guildMessageChannel, Long startId, InteractionHook hook, Member memberToPurgeMessagesOf);
}
