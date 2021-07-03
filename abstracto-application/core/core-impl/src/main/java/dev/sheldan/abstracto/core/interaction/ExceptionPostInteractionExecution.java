package dev.sheldan.abstracto.core.interaction;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.listener.async.jda.ButtonClickedListener;
import dev.sheldan.abstracto.core.models.listener.ButtonClickedListenerModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExceptionPostInteractionExecution implements PostInteractionExecution {

    @Autowired
    private ExceptionService exceptionService;

    @Override
    public void execute(ButtonClickedListenerModel interActionContext, InteractionResult interactionResult, ButtonClickedListener executedListener) {
        InteractionResultState result = interactionResult.getResult();
        if(result.equals(InteractionResultState.ERROR)) {
            Throwable throwable = interactionResult.getThrowable();
            if(throwable != null) {
                log.info("Exception handling in interaction for exception {}.", throwable.getClass().getSimpleName());
                exceptionService.reportExceptionToInteraction(throwable, interActionContext, executedListener);
            }
        }
    }
}
