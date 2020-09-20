package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.service.UndoActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UndoActionPostExecution implements PostCommandExecution {

    @Autowired
    private UndoActionService undoActionService;

    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        ResultState result = commandResult.getResult();
        if(result.equals(ResultState.ERROR) && commandContext.getUndoActions() != null && !commandContext.getUndoActions().isEmpty()) {
            undoActionService.performActionsFuture(commandContext.getUndoActions()).whenComplete((aVoid, undoThrowable) -> {
                if(undoThrowable != null) {
                    log.warn("Undo actions failed.", undoThrowable);
                } else {
                    log.info("Successfully executed undo actions.");
                }
            });
        }
    }
}
