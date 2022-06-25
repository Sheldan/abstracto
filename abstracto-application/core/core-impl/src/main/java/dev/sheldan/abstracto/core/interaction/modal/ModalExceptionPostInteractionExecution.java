package dev.sheldan.abstracto.core.interaction.modal;

import dev.sheldan.abstracto.core.interaction.InteractionExceptionService;
import dev.sheldan.abstracto.core.interaction.InteractionResult;
import dev.sheldan.abstracto.core.interaction.InteractionResultState;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListener;
import dev.sheldan.abstracto.core.interaction.modal.listener.ModalInteractionListenerModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ModalExceptionPostInteractionExecution implements ModalPostInteractionExecution {

    @Autowired
    private InteractionExceptionService interactionExceptionService;

    @Override
    public void execute(ModalInteractionListenerModel interActionContext, InteractionResult interactionResult, ModalInteractionListener executedListener) {
        InteractionResultState result = interactionResult.getResult();
        if(result.equals(InteractionResultState.ERROR)) {
            Throwable throwable = interactionResult.getThrowable();
            if(throwable != null) {
                log.info("Exception handling in modal interaction for exception {}.", throwable.getClass().getSimpleName());
                interactionExceptionService.reportExceptionToInteraction(throwable, interActionContext, executedListener);
            }
        }
    }
}
