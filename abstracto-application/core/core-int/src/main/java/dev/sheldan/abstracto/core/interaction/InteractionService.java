package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface InteractionService {
    List<CompletableFuture<Message>> sendMessageToInteraction(MessageToSend messageToSend, InteractionHook interactionHook);
    List<CompletableFuture<Message>> sendMessageToInteraction(String templateKey, Object model, InteractionHook interactionHook);
}
