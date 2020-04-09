package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.service.ChannelGroupCommandService;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.models.dto.CommandDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommandDisabledCondition implements CommandCondition {

    @Autowired
    private ChannelGroupCommandService channelGroupCommandService;

    @Autowired
    private CommandService commandManagementService;

    @Override
    public ConditionResult shouldExecute(CommandContext context, Command command) {
        CommandDto commandDto = commandManagementService.findCommandByName(command.getConfiguration().getName());
        Boolean booleanResult = channelGroupCommandService.isCommandEnabled(commandDto, context.getUserInitiatedContext().getChannel());
        return ConditionResult.builder().result(booleanResult).reason("Command is disabled.").build();
    }
}
