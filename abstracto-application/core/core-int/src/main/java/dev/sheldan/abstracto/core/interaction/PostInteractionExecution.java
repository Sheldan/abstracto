package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.listener.async.jda.ButtonClickedListener;
import dev.sheldan.abstracto.core.models.listener.ButtonClickedListenerModel;

public interface PostInteractionExecution {
    void execute(ButtonClickedListenerModel interActionContext, InteractionResult interactionResult, ButtonClickedListener executedListener);
}
