package dev.sheldan.abstracto.core.command.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import org.springframework.stereotype.Component;

@Component
public class MessageCommandCondition implements CommandCondition {

    @Override
    public ConditionResult shouldExecute(CommandContext commandContext, Command command) {
        if(command.getConfiguration().isSupportsMessageCommand()) {
            return ConditionResult.builder().result(true).build();
        } else {
            return ConditionResult.builder().result(false).reportResult(false).build();
        }
    }
}
