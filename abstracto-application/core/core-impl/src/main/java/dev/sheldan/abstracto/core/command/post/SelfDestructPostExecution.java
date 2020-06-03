package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import org.springframework.stereotype.Component;

@Component
public class SelfDestructPostExecution implements PostCommandExecution {
    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        if(commandResult.getResult().equals(ResultState.SELF_DESTRUCT)) {
            commandContext.getMessage().delete().queue();
        }
    }
}
