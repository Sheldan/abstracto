package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.function.Consumer;

public interface InteractiveService {
    void createMessageWithResponse(String templateKey, AUserInAServer responder, AChannel channel, Long messageId, Consumer<MessageReceivedEvent> action, Runnable finalAction);
    void createMessageWithResponse(MessageToSend messageToSend, AUserInAServer responder, AChannel channel, Long messageId, Consumer<MessageReceivedEvent> action, Runnable finalAction);
    void createMessageWithConfirmation(String text, AUserInAServer responder, AChannel channel, Long messageId, Consumer<Void> confirmation, Consumer<Void> denial, Runnable finalAction);
}
