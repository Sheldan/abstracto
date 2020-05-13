package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.exception.RoleException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class SetExpRole extends AbstractConditionableCommand {

    @Autowired
    private ExperienceRoleService experienceRoleService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private RoleService roleService;


    @Override
    public CommandResult execute(CommandContext commandContext) {
        Integer level = (Integer) commandContext.getParameters().getParameters().get(0);
        Long roleId = (Long) commandContext.getParameters().getParameters().get(1);
        Optional<ARole> roleOpt = roleManagementService.findRole(roleId, commandContext.getUserInitiatedContext().getServer());
        if(roleOpt.isPresent()) {
            ARole role = roleOpt.get();
            AServer server = commandContext.getUserInitiatedContext().getServer();
            if(!roleService.isRoleInServer(role)) {
                throw new RoleException("Role not found.");
            }
            log.info("Setting role  {} to be used for level {} on server {}", roleId, level, server.getId());
            experienceRoleService.setRoleToLevel(role, level, server, commandContext.getUserInitiatedContext().getChannel());
            return CommandResult.fromSuccess();
        }
       return CommandResult.fromError("Could not find role");
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("level").type(Integer.class).build());
        parameters.add(Parameter.builder().name("role").type(ARole.class).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("setExpRole")
                .module(ExperienceModule.EXPERIENCE)
                .templated(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }
}
