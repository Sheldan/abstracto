package dev.sheldan.abstracto.commands.management.post;

import dev.sheldan.abstracto.command.Command;
import dev.sheldan.abstracto.command.PostCommandExecution;
import dev.sheldan.abstracto.command.execution.CommandContext;
import dev.sheldan.abstracto.command.execution.Result;
import dev.sheldan.abstracto.command.execution.ResultState;
import org.springframework.stereotype.Service;

@Service
public class ReactionPostExecution implements PostCommandExecution {
    @Override
    public void execute(CommandContext commandContext, Result result, Command command) {
        if(result.getResult().equals(ResultState.ERROR)) {
            commandContext.getMessage().addReaction("⚠️").queue();
            if(result.getMessage() != null && result.getThrowable() == null){
                commandContext.getChannel().sendMessage(result.getMessage()).queue();
            }
        } else {
            if(command.getConfiguration().isCausesReaction()){
                commandContext.getMessage().addReaction("✅").queue();
            }
        }

    }
}
