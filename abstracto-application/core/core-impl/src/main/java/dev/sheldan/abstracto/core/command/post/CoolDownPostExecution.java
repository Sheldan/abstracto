package dev.sheldan.abstracto.core.command.post;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.command.service.CommandCoolDownService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CoolDownPostExecution implements PostCommandExecution {

    @Autowired
    private CommandCoolDownService commandCoolDownService;

    @Override
    @Transactional
    public void execute(CommandContext commandContext, CommandResult commandResult, Command command) {
        ResultState result = commandResult.getResult();
        if(result.equals(ResultState.SUCCESSFUL) || result.equals(ResultState.IGNORED)) {
            commandCoolDownService.updateCoolDowns(command, commandContext);
        }
    }

    @Override
    public boolean supportsSlash() {
        return true;
    }

    @Override
    public void executeSlash(SlashCommandInteractionEvent interaction, CommandResult commandResult, Command command) {
        ResultState result = commandResult.getResult();
        if(result.equals(ResultState.SUCCESSFUL) || result.equals(ResultState.IGNORED)) {
            commandCoolDownService.updateCoolDowns(command, interaction);
        }
    }
}
