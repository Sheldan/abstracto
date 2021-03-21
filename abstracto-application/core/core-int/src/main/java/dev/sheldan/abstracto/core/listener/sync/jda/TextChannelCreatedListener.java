package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.core.models.listener.TextChannelCreatedModel;

public interface TextChannelCreatedListener extends FeatureAwareListener<TextChannelCreatedModel, DefaultListenerResult>, Prioritized {
}
