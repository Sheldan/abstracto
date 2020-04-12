package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatures;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UnSetRole extends AbstractConditionableCommand {

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private ExperienceRoleService experienceRoleService;


    @Override
    public CommandResult execute(CommandContext commandContext) {
        Long roleId = (Long) commandContext.getParameters().getParameters().get(0);
        ARole role = roleManagementService.findRole(roleId);
        experienceRoleService.unsetRole(role, commandContext.getUserInitiatedContext().getServer());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("roleId").type(Long.class).build());
        HelpInfo helpInfo = HelpInfo.builder().longHelp("Removes the role from the experience tracking").usage("unsetRole <roleId>").build();
        return CommandConfiguration.builder()
                .name("unsetRole")
                .module(ExperienceModule.EXPERIENCE)
                .description("Removes the role from experience tracking")
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public String getFeature() {
        return ExperienceFeatures.EXPERIENCE;
    }
}
