package dev.sheldan.abstracto.assignableroles.command;

import dev.sheldan.abstracto.assignableroles.config.AssignableRoleFeatureDefinition;
import dev.sheldan.abstracto.assignableroles.service.AssignableRolePlaceService;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to remove a {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRole role}
 * from an {@link dev.sheldan.abstracto.assignableroles.model.database.AssignableRolePlace place}
 */
@Component
public class RemoveRoleFromAssignableRolePlace extends AbstractConditionableCommand {

    @Autowired
    private AssignableRolePlaceService service;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        String name = (String) parameters.get(0);
        ARole role = (ARole) parameters.get(1);
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        return service.removeRoleFromAssignableRolePlace(server, name, role)
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter rolePostName = Parameter
                .builder()
                .name("name")
                .type(String.class)
                .templated(true)
                .build();
        Parameter role = Parameter
                .builder()
                .name("role")
                .type(ARole.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(rolePostName, role);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name("removeRoleFromAssignableRolePlace")
                .module(AssignableRoleModuleDefinition.ASSIGNABLE_ROLES)
                .templated(true)
                .causesReaction(true)
                .async(true)
                .messageCommandOnly(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return AssignableRoleFeatureDefinition.ASSIGNABLE_ROLES;
    }
}
