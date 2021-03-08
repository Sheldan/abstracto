package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import net.dv8tion.jda.api.entities.Message;

public interface PrivateMessageReceivedListener extends FeatureAware, Prioritized {
    void execute(Message message);
}
