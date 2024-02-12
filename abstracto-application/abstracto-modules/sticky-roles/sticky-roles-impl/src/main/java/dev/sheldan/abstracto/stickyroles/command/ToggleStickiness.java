package dev.sheldan.abstracto.stickyroles.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.stickyroles.config.StickyRoleFeatureMode;
import dev.sheldan.abstracto.stickyroles.config.StickyRolesFeatureDefinition;
import dev.sheldan.abstracto.stickyroles.config.StickyRolesSlashCommandNames;
import dev.sheldan.abstracto.stickyroles.service.StickyRoleService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ToggleStickiness extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "toggleStickiness";
    private static final String RESPONSE_TEMPLATE = "toggleStickiness_response";

    private static final String STICKY_PARAMETER_NAME = "sticky";

    @Autowired
    private StickyRoleService stickyRoleService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;


    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Member targetMember = event.getMember();
        Boolean newState = slashCommandParameterService.getCommandOption(STICKY_PARAMETER_NAME, event, Boolean.class);
        stickyRoleService.setStickiness(targetMember, newState);
        return interactionService.replyEmbed(RESPONSE_TEMPLATE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter stateParameter = Parameter
                .builder()
                .name(STICKY_PARAMETER_NAME)
                .type(Boolean.class)
                .optional(false)
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(StickyRolesSlashCommandNames.STICKY_ROLES_PUBLIC)
                .commandName("toggle")
                .build();

        List<Parameter> parameters = Arrays.asList(stateParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name(COMMAND_NAME)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .parameters(parameters)
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

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(StickyRoleFeatureMode.ALLOW_SELF_MANAGEMENT);
    }
}
