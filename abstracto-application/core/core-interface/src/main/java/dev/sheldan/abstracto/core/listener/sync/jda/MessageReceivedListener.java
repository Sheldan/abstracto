package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.execution.result.MessageReceivedListenerResult;
import dev.sheldan.abstracto.core.listener.Consumable;
import net.dv8tion.jda.api.entities.Message;

public interface MessageReceivedListener extends FeatureAware, Prioritized, Consumable {
    MessageReceivedListenerResult execute(Message message);
}
