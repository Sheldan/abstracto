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
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class Close extends AbstractConditionableCommand {

    @Autowired
    private RequiresModMailCondition requiresModMailCondition;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private TemplateService templateService;


    @Override
    @Transactional
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String note = parameters.size() == 1 ? (String) parameters.get(0) : templateService.renderTemplate("modmail_close_default_note", new Object());
        ModMailThread thread = modMailThreadManagementService.getByChannel(commandContext.getUserInitiatedContext().getChannel());
        modMailThreadService.closeModMailThread(thread, commandContext.getChannel(), note);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter note = Parameter.builder().name("note").type(String.class).remainder(true).optional(true).build();
        List<Parameter> parameters = Arrays.asList(note);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("close")
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
