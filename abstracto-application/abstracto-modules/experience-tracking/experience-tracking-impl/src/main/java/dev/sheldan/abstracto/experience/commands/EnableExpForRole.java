package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Command used to remove a role from the list of roles for which experience is disabled
 */
@Component
public class EnableExpForRole extends AbstractConditionableCommand {

    @Autowired
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        ARole role = (ARole) commandContext.getParameters().getParameters().get(0);
        ARole actualRole = roleManagementService.findRole(role.getId());
        // If its not disabled for the role, we can remove it
        if(disabledExpRoleManagementService.isExperienceDisabledForRole(actualRole)) {
            disabledExpRoleManagementService.removeRoleToBeDisabledForExp(actualRole);
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("role").templated(true).type(ARole.class).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("enableExpForRole")
                .module(ExperienceModule.EXPERIENCE)
                .templated(true)
                .causesReaction(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }
}
