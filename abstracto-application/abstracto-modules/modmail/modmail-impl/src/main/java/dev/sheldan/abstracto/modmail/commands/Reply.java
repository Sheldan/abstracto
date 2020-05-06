package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.modmail.commands.condition.RequiresModMailCondition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Reply extends AbstractConditionableCommand {

    @Autowired
    private RequiresModMailCondition requiresModMailCondition;

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        ModMailThread thread = modMailThreadManagementService.getByChannel(commandContext.getUserInitiatedContext().getChannel());
        modMailThreadService.relayMessageToDm(thread, commandContext.getMessage(), false);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter responseText = Parameter.builder().name("text").type(String.class).description("The text to reply with").build();
        List<Parameter> parameters = Arrays.asList(responseText);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("reply")
                .module(ModMailModuleInterface.MODMAIL)
                .parameters(parameters)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MODMAIL;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(requiresModMailCondition);
        return conditions;
    }
}
