package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.models.listener.GuildMessageEmbedEventModel;

public interface MessageEmbeddedListener extends FeatureAware, Prioritized {
    void execute(GuildMessageEmbedEventModel eventModel);
}
