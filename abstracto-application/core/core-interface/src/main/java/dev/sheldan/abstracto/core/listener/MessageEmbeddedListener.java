package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.models.listener.GuildMessageEmbedEventModel;

public interface MessageEmbeddedListener extends FeatureAware, Prioritized {
    void execute(GuildMessageEmbedEventModel eventModel);
}
