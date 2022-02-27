package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;

import java.util.function.Consumer;

public interface InteractiveService {
    void createMessageWithResponse(String templateKey, AUserInAServer responder, AChannel channel, Consumer<MessageReceivedModel> action, Consumer<MessageReceivedModel> finalAction);
    void createMessageWithResponse(MessageToSend messageToSend, AUserInAServer responder, AChannel channel, Consumer<MessageReceivedModel> action, Consumer<MessageReceivedModel> finalAction);
}
