package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.core.models.listener.EmoteDeletedModel;

public interface EmoteDeletedListener extends FeatureAwareListener<EmoteDeletedModel, DefaultListenerResult>, Prioritized {
}
