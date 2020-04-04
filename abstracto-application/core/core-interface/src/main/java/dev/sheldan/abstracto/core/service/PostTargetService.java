package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.models.MessageToSend;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PostTargetService {
    CompletableFuture<Message> sendTextInPostTarget(String text, PostTarget target);
    CompletableFuture<Message> sendEmbedInPostTarget(MessageEmbed embed, PostTarget target);
    CompletableFuture<Message> sendTextInPostTarget(String text, String postTargetName, Long serverId);
    CompletableFuture<Message> sendEmbedInPostTarget(MessageEmbed embed, String postTargetName, Long serverId);
    CompletableFuture<Message> sendEmbedInPostTarget(MessageToSend message, String postTargetName, Long serverId);
    CompletableFuture<Message> sendEmbedInPostTarget(MessageToSend message, PostTarget target);
    CompletableFuture<Message> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTarget target);
    CompletableFuture<Message> editEmbedInPostTarget(Long messageId, MessageToSend message, String postTargetName, Long serverId);
    void editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTarget target, CompletableFuture<Message> future);
    void editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, String postTarget, Long serverId, CompletableFuture<Message> future);
    void throwIfPostTargetIsNotDefined(String name, Long serverId);
    boolean validPostTarget(String name);
    List<String> getAvailablePostTargets();
}
