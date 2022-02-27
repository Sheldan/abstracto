package dev.sheldan.abstracto.core.listener.async;

import dev.sheldan.abstracto.core.interaction.MessageContextConfig;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;

public interface MessageContextCommandListener extends FeatureAwareListener<MessageContextInteractionModel, DefaultListenerResult> {
    MessageContextConfig getConfig();
    Boolean handlesEvent(MessageContextInteractionModel model);
}
