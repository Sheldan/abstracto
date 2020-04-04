package dev.sheldan.abstracto.core.listener;

import net.dv8tion.jda.api.entities.Message;

public interface MessageReceivedListener extends FeatureAware {
    void execute(Message message);
}
