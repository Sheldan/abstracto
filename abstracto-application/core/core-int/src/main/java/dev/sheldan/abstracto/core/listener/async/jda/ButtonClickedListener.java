package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;
import dev.sheldan.abstracto.core.models.listener.ButtonClickedListenerModel;

public interface ButtonClickedListener extends FeatureAwareListener<ButtonClickedListenerModel, ButtonClickedListenerResult>, Prioritized {
    Boolean handlesEvent(ButtonClickedListenerModel model);
}
