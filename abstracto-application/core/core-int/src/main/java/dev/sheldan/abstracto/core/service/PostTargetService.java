package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface PostTargetService {
    CompletableFuture<Message> sendTextInPostTarget(String text, PostTarget target);
    CompletableFuture<Message> sendEmbedInPostTarget(MessageEmbed embed, PostTarget target);
    CompletableFuture<Message> sendTextInPostTarget(String text, PostTargetEnum postTargetName, Long serverId);
    CompletableFuture<Message> sendEmbedInPostTarget(MessageEmbed embed, PostTargetEnum postTargetName, Long serverId);
    CompletableFuture<Message> sendMessageInPostTarget(Message message, PostTargetEnum postTargetName,  Long serverId);
    CompletableFuture<Message> sendMessageInPostTarget(Message message, PostTarget target);
    List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageToSend message, PostTargetEnum postTargetName, Long serverId);
    List<CompletableFuture<Message>> sendEmbedInPostTarget(MessageToSend message, PostTarget target);
    List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTarget target);
    List<CompletableFuture<Message>> editEmbedInPostTarget(Long messageId, MessageToSend message, PostTargetEnum postTargetName, Long serverId);
    List<CompletableFuture<Message>> editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTarget target);
    List<CompletableFuture<Message>> editOrCreatedInPostTarget(Long messageId, MessageToSend messageToSend, PostTargetEnum postTarget, Long serverId);
    void throwIfPostTargetIsNotDefined(PostTargetEnum targetEnum, Long serverId);
    boolean postTargetDefinedInServer(PostTargetEnum targetEnum, Long serverId);
    boolean validPostTarget(String name);
    void validatePostTarget(PostTargetEnum targetEnum, Long serverId);
    boolean postTargetUsableInServer(PostTargetEnum targetEnum, Long serverId);
    List<PostTarget> getPostTargets(AServer server);
    List<String> getAvailablePostTargets();
    List<String> getPostTargetsOfEnabledFeatures(AServer server);
    void disablePostTarget(String name, Long serverId);
    void enablePostTarget(String name, Long serverId);
    Optional<GuildMessageChannel> getPostTargetChannel(PostTargetEnum postTargetEnum, Long serverId);
}
