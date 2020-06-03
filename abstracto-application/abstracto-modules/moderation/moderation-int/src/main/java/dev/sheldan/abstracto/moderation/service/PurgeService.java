package dev.sheldan.abstracto.moderation.service;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.concurrent.CompletableFuture;

public interface PurgeService {
    CompletableFuture<Void> purgeMessagesInChannel(Integer count, TextChannel channel, Long messageId, Member purgingRestriction);
    CompletableFuture<Void> purgeMessagesInChannel(Integer count, TextChannel channel, Message origin, Member purgingRestriction);
}
