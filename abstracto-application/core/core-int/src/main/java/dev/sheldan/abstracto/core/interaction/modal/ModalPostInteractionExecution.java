package dev.sheldan.abstracto.core.interaction.modal;

import dev.sheldan.abstracto.core.interaction.InteractionResult;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerModel;

public interface ModalPostInteractionExecution {
    void execute(ModalInteractionListenerModel interActionContext, InteractionResult interactionResult, ModalInteractionListener executedListener);
}
