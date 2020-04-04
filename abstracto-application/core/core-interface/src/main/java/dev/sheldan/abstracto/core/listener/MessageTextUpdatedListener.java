package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.CachedMessage;
import net.dv8tion.jda.api.entities.Message;

public interface MessageTextUpdatedListener extends FeatureAware {
    void execute(CachedMessage messageBefore, Message messageAfter);
}
