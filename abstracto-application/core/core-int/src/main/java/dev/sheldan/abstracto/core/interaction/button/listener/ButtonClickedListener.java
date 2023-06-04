package dev.sheldan.abstracto.core.interaction.button.listener;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.interaction.InteractionListener;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;

public interface ButtonClickedListener extends FeatureAwareListener<ButtonClickedListenerModel, ButtonClickedListenerResult>, Prioritized, InteractionListener {
    Boolean handlesEvent(ButtonClickedListenerModel model);
    default Boolean autoAcknowledgeEvent() {
        return true;
    }
}
