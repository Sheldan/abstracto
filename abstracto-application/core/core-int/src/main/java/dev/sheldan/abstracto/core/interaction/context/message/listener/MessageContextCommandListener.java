package dev.sheldan.abstracto.core.interaction.context.message.listener;

import dev.sheldan.abstracto.core.interaction.InteractionListener;
import dev.sheldan.abstracto.core.interaction.MessageContextConfig;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;

public interface MessageContextCommandListener extends FeatureAwareListener<MessageContextInteractionModel, DefaultListenerResult>, InteractionListener {
    MessageContextConfig getConfig();
    Boolean handlesEvent(MessageContextInteractionModel model);
}
