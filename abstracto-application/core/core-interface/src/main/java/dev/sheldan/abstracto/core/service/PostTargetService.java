package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.PostTarget;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.concurrent.CompletableFuture;

public interface PostTargetService {
    CompletableFuture<Message> sendTextInPostTarget(String text, PostTarget target);
    CompletableFuture<Message> sendEmbedInPostTarget(MessageEmbed embed, PostTarget target);
    CompletableFuture<Message> sendTextInPostTarget(String text, String postTargetName, Long serverId);
    CompletableFuture<Message> sendEmbedInPostTarget(MessageEmbed embed, String postTargetName, Long serverId);
}
