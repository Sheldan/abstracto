package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SelfDestructPostExecution implements PostCommandExecution {

    @Autowired
    private MessageService messageService;

    @Override
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        if(commandResult.getResult().equals(ResultState.SELF_DESTRUCT)) {
            Message message = commandContext.getMessage();
            log.debug("Command {} is of type self destruct. Deleting message {} in channel {} in server {}.",
                    command.getConfiguration().getName(), message.getId(), message.getChannel().getId(), message.getGuild().getId());
            messageService.deleteMessage(message);
        }
    }
}
