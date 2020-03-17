package dev.sheldan.abstracto.commands.management;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.PostCommandExecution;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Result;
import org.springframework.stereotype.Service;

@Service
public class ReactionPostExecution implements PostCommandExecution {
    @Override
    public void execute(CommandContext commandContext, Result result, Command command) {
        if(command.getConfiguration().isCausesReaction()){
            commandContext.getMessage().addReaction("⭐").queue();
        }
    }
}
