package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.FeatureAware;
import dev.sheldan.abstracto.core.models.listener.GuildMessageEmbedEventModel;

public interface AsyncMessageEmbeddedListener extends FeatureAware {
    void execute(GuildMessageEmbedEventModel eventModel);
}
