package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.starboard.model.StarboardPostDeletedModel;

public interface StarboardPostDeletedListener extends FeatureAwareListener<StarboardPostDeletedModel, DefaultListenerResult> {
}
