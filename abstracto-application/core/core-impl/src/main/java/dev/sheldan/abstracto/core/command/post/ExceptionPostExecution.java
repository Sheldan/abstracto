package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExceptionPostExecution implements PostCommandExecution {

    @Autowired
    private ExceptionService exceptionService;

    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        ResultState result = commandResult.getResult();
        if(result.equals(ResultState.ERROR)) {
            Throwable throwable = commandResult.getThrowable();
            if(throwable != null) {
                log.info("Exception handling for exception {}.", throwable.getClass().getSimpleName());
                exceptionService.reportExceptionToContext(throwable, commandContext, command);
            }
        }
    }
}
