package dev.sheldan.abstracto.core.listener;

import net.dv8tion.jda.api.entities.Message;

public interface MessageReceivedListener extends FeatureAware, Prioritized {
    void execute(Message message);
}
