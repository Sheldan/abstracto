package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.model.condition.AssignableRoleConditionType;
import dev.sheldan.abstracto.assignableroles.service.AssignableRoleConditionService;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RemoveAssignableRoleCondition extends AbstractConditionableCommand {

    @Autowired
    private AssignableRoleConditionService assignableRoleConditionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String name = (String) parameters.get(0);
        Role role = (Role) parameters.get(1);
        AssignableRoleConditionType configKey = (AssignableRoleConditionType) parameters.get(2);
        assignableRoleConditionService.deleteAssignableRoleCondition(name, role, configKey);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter placeName = Parameter
                .builder()
                .name("name")
                .type(String.class)
                .templated(true)
                .build();
        Parameter role = Parameter
                .builder()
                .name("role")
                .type(Role.class)
                .templated(true)
                .build();
        Parameter conditionKey = Parameter
                .builder()
                .name("conditionKey")
                .type(AssignableRoleConditionType.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(placeName, role, conditionKey);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("removeAssignableRoleCondition")
                .module(AssignableRoleModuleDefinition.ASSIGNABLE_ROLES)
                .templated(true)
                .messageCommandOnly(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return AssignableRoleFeatureDefinition.ASSIGNABLE_ROLES;
    }
}
