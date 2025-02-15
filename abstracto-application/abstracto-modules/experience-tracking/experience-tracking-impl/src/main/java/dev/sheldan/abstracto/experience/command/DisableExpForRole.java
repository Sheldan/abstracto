package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to add a role to the roles for which experience has been disabled.
 */
@Component
public class DisableExpForRole extends AbstractConditionableCommand {

    private static final String DISABLE_EXP_FOR_ROLE_COMMAND = "disableExpForRole";
    private static final String DISABLE_EXP_FOR_ROLE_RESPONSE = "disableExpForRole_response";
    private static final String ROLE_PARAMETER = "role";
    @Autowired
    private DisabledExpRoleManagementService disabledExpRoleManagementService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;


    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Role role = slashCommandParameterService.getCommandOption(ROLE_PARAMETER, event, Role.class);
        ARole actualRole = roleManagementService.findRole(role.getIdLong());
        if(!actualRole.getServer().getId().equals(event.getGuild().getIdLong())) {
            throw new EntityGuildMismatchException();
        }
        // as we manage experience disabled roles via the existence of them in a table, we should not do anything
        // in case it is used a second time as a disabled experience role
        if(!disabledExpRoleManagementService.isExperienceDisabledForRole(actualRole)) {
            disabledExpRoleManagementService.setRoleToBeDisabledForExp(actualRole);
        }
        return interactionService.replyEmbed(DISABLE_EXP_FOR_ROLE_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter roleParameter = Parameter
                .builder()
                .name(ROLE_PARAMETER)
                .templated(true)
                .type(Role.class)
                .build();
        List<Parameter> parameters = Arrays.asList(roleParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ExperienceSlashCommandNames.EXPERIENCE_CONFIG)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .commandName(DISABLE_EXP_FOR_ROLE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(DISABLE_EXP_FOR_ROLE_COMMAND)
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .templated(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
