package dev.sheldan.abstracto.core.interaction.button;

import dev.sheldan.abstracto.core.interaction.InteractionResult;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;

public interface ButtonPostInteractionExecution {
    void execute(ButtonClickedListenerModel interActionContext, InteractionResult interactionResult, ButtonClickedListener executedListener);
}
