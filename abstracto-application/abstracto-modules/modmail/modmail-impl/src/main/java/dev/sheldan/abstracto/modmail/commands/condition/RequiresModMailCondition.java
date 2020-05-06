package dev.sheldan.abstracto.modmail.commands.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequiresModMailCondition implements CommandCondition {

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Override
    public ConditionResult shouldExecute(CommandContext commandContext, Command command) {
        ModMailThread thread = modMailThreadManagementService.getByChannel(commandContext.getUserInitiatedContext().getChannel());
        if(thread != null) {
            return ConditionResult.builder().result(true).build();
        }
        return ConditionResult.builder().result(false).reason("Not in a mod mail thread.").build();
    }
}
