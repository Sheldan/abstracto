package dev.sheldan.abstracto.stickyroles.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.stickyroles.config.StickyRolesFeatureDefinition;
import dev.sheldan.abstracto.stickyroles.config.StickyRolesSlashCommandNames;
import dev.sheldan.abstracto.stickyroles.service.StickyRoleService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ConfigureStickyRole extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "configureStickyRole";
    private static final String STICKY_PARAMETER_NAME = "sticky";
    private static final String ROLE_PARAMETER_NAME = "role";
    private static final String RESPONSE_TEMPLATE = "configureStickyRole_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private StickyRoleService stickyRoleService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Boolean newState = slashCommandParameterService.getCommandOption(STICKY_PARAMETER_NAME, event, Boolean.class);
        Role role = slashCommandParameterService.getCommandOption(ROLE_PARAMETER_NAME, event, Role.class);
        stickyRoleService.setRoleStickiness(role, newState);
        return interactionService.replyEmbed(RESPONSE_TEMPLATE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter roleParameter = Parameter
                .builder()
                .name(ROLE_PARAMETER_NAME)
                .type(Role.class)
                .optional(false)
                .templated(true)
                .build();

        Parameter stateParameter = Parameter
                .builder()
                .name(STICKY_PARAMETER_NAME)
                .type(Boolean.class)
                .optional(false)
                .templated(true)
                .build();

        List<Parameter> parameters = Arrays.asList(roleParameter, stateParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(StickyRolesSlashCommandNames.STICKY_ROLES)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .commandName("configure")
                .build();

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name(COMMAND_NAME)
                .module(UtilityModuleDefinition.UTILITY)
                .parameters(parameters)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return StickyRolesFeatureDefinition.STICKY_ROLES;
    }
}
