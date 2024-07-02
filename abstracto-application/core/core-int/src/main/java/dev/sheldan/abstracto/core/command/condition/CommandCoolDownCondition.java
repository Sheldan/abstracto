package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.CommandCoolDownDetail;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CoolDownCheckResult;
import dev.sheldan.abstracto.core.command.service.CommandCoolDownService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandCoolDownCondition implements CommandCondition {

    @Autowired
    private CommandCoolDownService commandCoolDownService;

    @Override
    public ConditionResult shouldExecute(CommandContext commandContext, Command command) {
        commandCoolDownService.takeLock();
        try {
            CoolDownCheckResult result = commandCoolDownService.allowedToExecuteCommand(command, commandContext);
            if (result.getCanExecute()) {
                return ConditionResult
                        .builder()
                        .result(true)
                        .build();
            } else {
                return ConditionResult
                        .builder()
                        .result(false)
                        .conditionDetail(new CommandCoolDownDetail(result))
                        .build();
            }
        } finally {
            commandCoolDownService.releaseLock();
        }
    }

    @Override
    public ConditionResult shouldExecute(SlashCommandInteractionEvent slashCommandInteractionEvent, Command command) {
        if(ContextUtils.isUserCommand(slashCommandInteractionEvent)) {
            return ConditionResult.SUCCESS;
        }
        commandCoolDownService.takeLock();
        try {
            CoolDownCheckResult result = commandCoolDownService.allowedToExecuteCommand(command, slashCommandInteractionEvent);
            if (result.getCanExecute()) {
                return ConditionResult
                        .builder()
                        .result(true)
                        .build();
            } else {
                return ConditionResult
                        .builder()
                        .result(false)
                        .conditionDetail(new CommandCoolDownDetail(result))
                        .build();
            }
        } finally {
            commandCoolDownService.releaseLock();
        }
    }

    @Override
    public boolean supportsSlashCommands() {
        return true;
    }
}
