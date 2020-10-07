package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SelfDestructPostExecution implements PostCommandExecution {
    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        if(commandResult.getResult().equals(ResultState.SELF_DESTRUCT)) {
            Message message = commandContext.getMessage();
            log.trace("Command {} is of type self destruct. Deleting message {} in channel {} in server {}.",
                    command.getConfiguration().getName(), message.getId(), message.getChannel().getId(), message.getGuild().getId());
            message.delete().queue();
        }
    }
}
