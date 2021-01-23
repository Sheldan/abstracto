package dev.sheldan.abstracto.modmail.commands.condition;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.modmail.condition.ModMailContextCondition;
import dev.sheldan.abstracto.modmail.condition.detail.NotInModMailThreadConditionDetail;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This {@link dev.sheldan.abstracto.core.command.condition.CommandCondition} checks the channel it is executed in
 * and checks if the channel is a valid and open mod mail thread.
 */
@Component
public class RequiresModMailCondition implements ModMailContextCondition {

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public ConditionResult shouldExecute(CommandContext commandContext, Command command) {
        Optional<ModMailThread> threadOptional = modMailThreadManagementService.getByChannelOptional(channelManagementService.loadChannel(commandContext.getChannel()));
        if(threadOptional.isPresent()) {
            return ConditionResult.builder().result(true).build();
        }
        return ConditionResult.builder().result(false).conditionDetail(new NotInModMailThreadConditionDetail()).build();
    }
}
