package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.moderation.model.listener.InfractionLevelChangedEventModel;

public interface InfractionLevelChangedListener extends FeatureAwareListener<InfractionLevelChangedEventModel, DefaultListenerResult> {
}
