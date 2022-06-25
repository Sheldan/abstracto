package dev.sheldan.abstracto.core.interaction.modal.listener;

import dev.sheldan.abstracto.core.Prioritized;
import dev.sheldan.abstracto.core.interaction.InteractionListener;
import dev.sheldan.abstracto.core.listener.FeatureAwareListener;

public interface ModalInteractionListener extends FeatureAwareListener<ModalInteractionListenerModel, ModalInteractionListenerResult>, Prioritized, InteractionListener {
    Boolean handlesEvent(ModalInteractionListenerModel model);
}
