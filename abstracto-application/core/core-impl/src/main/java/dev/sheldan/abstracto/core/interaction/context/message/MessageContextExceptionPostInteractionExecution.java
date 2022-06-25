package dev.sheldan.abstracto.core.interaction.context.message;

import dev.sheldan.abstracto.core.interaction.InteractionExceptionService;
import dev.sheldan.abstracto.core.interaction.InteractionResult;
import dev.sheldan.abstracto.core.interaction.InteractionResultState;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageContextExceptionPostInteractionExecution implements MessageContextPostInteractionExecution {

    @Autowired
    private InteractionExceptionService interactionExceptionService;

    @Override
    public void execute(MessageContextInteractionModel interActionContext, InteractionResult interactionResult, MessageContextCommandListener executedListener) {
        InteractionResultState result = interactionResult.getResult();
        if(result.equals(InteractionResultState.ERROR)) {
            Throwable throwable = interactionResult.getThrowable();
            if(throwable != null) {
                log.info("Exception handling in message context interaction for exception {}.", throwable.getClass().getSimpleName());
                interactionExceptionService.reportExceptionToInteraction(throwable, interActionContext, executedListener);
            }
        }
    }
}
