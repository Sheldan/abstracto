package dev.sheldan.abstracto.starboard.listener;

import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.starboard.model.StarboardPostUpdatedModel;

public interface StarboardPostUpdatedListener extends FeatureAwareListener<StarboardPostUpdatedModel, DefaultListenerResult> {
}
