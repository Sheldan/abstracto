package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.listener.ConsumableListenerResult;
import dev.sheldan.abstracto.core.listener.Consumable;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;

public interface MessageReceivedListener extends FeatureAwareListener<MessageReceivedModel, ConsumableListenerResult>, Prioritized, Consumable {
}
