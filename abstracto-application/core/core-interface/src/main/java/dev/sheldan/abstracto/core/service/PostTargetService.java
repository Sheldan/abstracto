package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.dto.PostTargetDto;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PostTargetService {
    CompletableFuture<Message> sendTextInPostTarget(String text, PostTargetDto target);
    CompletableFuture<Message> sendEmbedInPostTarget(MessageEmbed embed, PostTargetDto target);
    CompletableFuture<Message> sendTextInPostTarget(String text, String postTargetName, Long serverId);
    CompletableFuture<Message> sendEmbedInPostTarget(MessageEmbed embed, String postTargetName, Long serverId);
    List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageToSend message, String postTargetName, Long serverId);
    List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageToSend message, PostTargetDto target);
    List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTargetDto target);
    List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, String postTargetName, Long serverId);
    void editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTargetDto target, List<CompletableFuture<Message>> future);
    void editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, String postTarget, Long serverId, List<CompletableFuture<Message>> future);
    void throwIfPostTargetIsNotDefined(String name, Long serverId);
    boolean validPostTarget(String name);
    List<String> getAvailablePostTargets();
    PostTargetDto getPostTarget(String name, Long serverId);
}
