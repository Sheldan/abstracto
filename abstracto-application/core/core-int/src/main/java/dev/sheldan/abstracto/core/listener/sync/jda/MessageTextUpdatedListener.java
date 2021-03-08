package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import net.dv8tion.jda.api.entities.Message;

public interface MessageTextUpdatedListener extends FeatureAware, Prioritized {
    void execute(CachedMessage messageBefore, Message messageAfter);
}