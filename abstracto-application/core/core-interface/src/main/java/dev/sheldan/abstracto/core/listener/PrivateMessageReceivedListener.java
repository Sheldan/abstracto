package dev.sheldan.abstracto.core.listener;

import net.dv8tion.jda.api.entities.Message;

public interface PrivateMessageReceivedListener extends FeatureAware, Prioritized {
    void execute(Message message);
}
