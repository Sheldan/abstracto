package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.exception.ExperienceRoleNotFoundException;
import dev.sheldan.abstracto.experience.model.database.AExperienceRole;
import dev.sheldan.abstracto.experience.service.ExperienceRoleService;
import dev.sheldan.abstracto.experience.service.management.ExperienceRoleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to remove a role from the roles to be awarded at certain levels. If there are users with this role currently, their role
 * will be recalculated, and this will be shown with an status update message.
 */
@Component
public class UnSetExpRole extends AbstractConditionableCommand {

    @Autowired
    private ExperienceRoleService experienceRoleService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private ExperienceRoleManagementService experienceRoleManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        ARole role = (ARole) commandContext.getParameters().getParameters().get(0);
        ARole actualRole = roleManagementService.findRole(role.getId());
        if(!actualRole.getServer().getId().equals(commandContext.getGuild().getIdLong())) {
            throw new EntityGuildMismatchException();
        }
        // do not check for the existence of the role, because if the role was deleted, users should be able
        // to get rid of it in the configuration
        Optional<AExperienceRole> experienceRole = experienceRoleManagementService.getRoleInServerOptional(actualRole);
        if(!experienceRole.isPresent()) {
            throw new ExperienceRoleNotFoundException();
        }
        return experienceRoleService.unsetRoles(actualRole, commandContext.getChannel())
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter roleParameter = Parameter
                .builder()
                .name("role")
                .templated(true)
                .type(ARole.class)
                .build();
        parameters.add(roleParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("unSetExpRole")
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .templated(true)
                .async(true)
                .messageCommandOnly(true)
                .causesReaction(true)
                .requiresConfirmation(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
