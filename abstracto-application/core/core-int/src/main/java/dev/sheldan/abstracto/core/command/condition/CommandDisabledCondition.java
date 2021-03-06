package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.detail.CommandDisabledDetail;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.service.ChannelGroupCommandService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandDisabledCondition implements CommandCondition {

    @Autowired
    private ChannelGroupCommandService channelGroupCommandService;

    @Autowired
    private CommandManagementService commandManagementService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        ACommand acommand = commandManagementService.findCommandByName(command.getConfiguration().getName());
        Boolean commandEnabled = channelGroupCommandService.isCommandEnabled(acommand, context.getUserInitiatedContext().getMessageChannel().getIdLong());
        if(!commandEnabled) {
            return ConditionResult.builder().result(false).conditionDetail(new CommandDisabledDetail()).build();
        }
        return ConditionResult.builder().result(true).build();
    }
}
