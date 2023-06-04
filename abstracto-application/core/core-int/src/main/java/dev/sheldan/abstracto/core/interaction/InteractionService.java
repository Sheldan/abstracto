package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface InteractionService {
    List<CompletableFuture<Message>> sendMessageToInteraction(MessageToSend messageToSend, InteractionHook interactionHook);
    List<CompletableFuture<Message>> sendMessageToInteraction(String templateKey, Object model, InteractionHook interactionHook);
    CompletableFuture<InteractionHook> replyEmbed(String templateKey, Object model, IReplyCallback callback);
    CompletableFuture<InteractionHook> replyString(String text, IReplyCallback callback);
    CompletableFuture<InteractionHook> replyEmbed(String templateKey, IReplyCallback callback);
    CompletableFuture<Message> editOriginal(MessageToSend messageToSend, InteractionHook interactionHook);
    CompletableFuture<InteractionHook> replyMessageToSend(MessageToSend messageToSend, IReplyCallback callback);
    CompletableFuture<InteractionHook> replyMessage(String templateKey, Object model, IReplyCallback callback);
    CompletableFuture<Message> replyString(String content, InteractionHook interactionHook);
}
