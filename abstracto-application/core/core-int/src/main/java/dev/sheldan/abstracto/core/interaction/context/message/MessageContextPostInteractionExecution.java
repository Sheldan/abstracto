package dev.sheldan.abstracto.core.interaction.context.message;

import dev.sheldan.abstracto.core.interaction.InteractionResult;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;

public interface MessageContextPostInteractionExecution {
    void execute(MessageContextInteractionModel interActionContext, InteractionResult interactionResult, MessageContextCommandListener executedListener);
}
