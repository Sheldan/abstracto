package dev.sheldan.abstracto.core.interaction.button;

import dev.sheldan.abstracto.core.interaction.InteractionExceptionService;
import dev.sheldan.abstracto.core.interaction.InteractionResult;
import dev.sheldan.abstracto.core.interaction.InteractionResultState;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListener;
import dev.sheldan.abstracto.core.interaction.button.listener.ButtonClickedListenerModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ButtonExceptionPostInteractionExecution implements ButtonPostInteractionExecution {

    @Autowired
    private InteractionExceptionService interactionExceptionService;

    @Override
    public void execute(ButtonClickedListenerModel interActionContext, InteractionResult interactionResult, ButtonClickedListener executedListener) {
        InteractionResultState result = interactionResult.getResult();
        if(result.equals(InteractionResultState.ERROR)) {
            Throwable throwable = interactionResult.getThrowable();
            if(throwable != null) {
                log.info("Exception handling in interaction for exception {}.", throwable.getClass().getSimpleName());
                interactionExceptionService.reportExceptionToInteraction(throwable, interActionContext, executedListener);
            }
        }
    }
}
