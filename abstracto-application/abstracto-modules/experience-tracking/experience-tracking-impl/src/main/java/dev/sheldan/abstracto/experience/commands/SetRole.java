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
public class SetRole extends AbstractConditionableCommand {

    @Autowired
    private ExperienceRoleService experienceRoleService;

    @Autowired
    private RoleManagementService roleManagementService;


    @Override
    public CommandResult execute(CommandContext commandContext) {
        Integer level = (Integer) commandContext.getParameters().getParameters().get(0);
        Long roleId = (Long) commandContext.getParameters().getParameters().get(1);
        ARole role = roleManagementService.findRole(roleId);
        experienceRoleService.setRoleToLevel(role, level, commandContext.getUserInitiatedContext().getServer());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("level").type(Integer.class).build());
        parameters.add(Parameter.builder().name("roleId").type(Long.class).build());
        HelpInfo helpInfo = HelpInfo.builder().longHelp("Sets the role to a certain level").usage("setRole <level> <roleId>").build();
        return CommandConfiguration.builder()
                .name("setRole")
                .module(ExperienceModule.EXPERIENCE)
                .description("Sets the role to a certain level")
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
