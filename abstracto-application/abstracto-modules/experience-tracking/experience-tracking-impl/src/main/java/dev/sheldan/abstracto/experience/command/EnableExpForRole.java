package dev.sheldan.abstracto.experience.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.exception.SlashCommandParameterMissingException;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatureDefinition;
import dev.sheldan.abstracto.experience.config.ExperienceSlashCommandNames;
import dev.sheldan.abstracto.experience.service.management.DisabledExpRoleManagementService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command used to remove a role from the list of roles for which experience is disabled
 */
@Component
public class EnableExpForRole extends AbstractConditionableCommand {

    private static final String ENABLE_EXP_FOR_ROLE_COMMAND = "enableExpForRole";
    private static final String ENABLE_EXP_FOR_ROLE_RESPONSE = "enableExpForRole_response";
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
        ARole actualRole;
        if(slashCommandParameterService.hasCommandOptionWithFullType(ROLE_PARAMETER, event, OptionType.ROLE)) {
            Role role = slashCommandParameterService.getCommandOption(ROLE_PARAMETER, event, ARole.class, Role.class);
            actualRole = roleManagementService.findRole(role.getIdLong());
        } else if(slashCommandParameterService.hasCommandOptionWithFullType(ROLE_PARAMETER, event, OptionType.STRING)) {
            String roleId = slashCommandParameterService.getCommandOption(ROLE_PARAMETER, event, ARole.class, String.class);
            actualRole = roleManagementService.findRole(Long.parseLong(roleId));
        } else {
            throw new SlashCommandParameterMissingException(ROLE_PARAMETER);
        }
        if(disabledExpRoleManagementService.isExperienceDisabledForRole(actualRole)) {
            disabledExpRoleManagementService.removeRoleToBeDisabledForExp(actualRole);
        }
        return interactionService.replyEmbed(ENABLE_EXP_FOR_ROLE_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter roleParameter = Parameter
                .builder()
                .name(ROLE_PARAMETER)
                .templated(true)
                .type(ARole.class)
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
                .commandName(ENABLE_EXP_FOR_ROLE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(ENABLE_EXP_FOR_ROLE_COMMAND)
                .module(ExperienceModuleDefinition.EXPERIENCE)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .causesReaction(true)
                .supportsEmbedException(true)
                .slashCommandOnly(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ExperienceFeatureDefinition.EXPERIENCE;
    }
}
