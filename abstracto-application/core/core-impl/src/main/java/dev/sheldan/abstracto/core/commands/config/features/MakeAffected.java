package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.service.RoleImmunityService;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class MakeAffected extends AbstractConditionableCommand {

    private static final String EFFECT_PARAMETER = "effect";
    private static final String ROLE_PARAMETER = "role";
    private static final String MAKE_AFFECTED_RESPONSE = "makeAffected_response";
    private static final String MAKE_AFFECTED_COMMAND_NAME = "makeAffected";
    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private RoleImmunityService roleImmunityService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String name = (String) commandContext.getParameters().getParameters().get(0);
        Role role = (Role) commandContext.getParameters().getParameters().get(1);
        if(!role.getGuild().getId().equals(commandContext.getGuild().getId())) {
            throw new EntityGuildMismatchException();
        }
        ARole aRole = roleManagementService.findRole(role.getIdLong());
        roleImmunityService.makeRoleAffected(aRole, name);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String name = slashCommandParameterService.getCommandOption(EFFECT_PARAMETER, event, String.class);
        Role role = slashCommandParameterService.getCommandOption(ROLE_PARAMETER, event, Role.class);
        if(!role.getGuild().getId().equals(event.getGuild().getId())) {
            throw new EntityGuildMismatchException();
        }
        ARole aRole = roleManagementService.findRole(role.getIdLong());
        roleImmunityService.makeRoleAffected(aRole, name);
        return interactionService.replyEmbed(MAKE_AFFECTED_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter
                .builder()
                .name(EFFECT_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();

        Parameter role = Parameter
                .builder()
                .name(ROLE_PARAMETER)
                .type(Role.class)
                .templated(true)
                .build();

        List<Parameter> parameters = Arrays.asList(featureName, role);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CoreSlashCommandNames.CONFIG)
                .commandName(MAKE_AFFECTED_COMMAND_NAME)
                .build();

        return CommandConfiguration.builder()
                .name(MAKE_AFFECTED_COMMAND_NAME)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .supportsEmbedException(true)
                .help(helpInfo)
                .slashCommandConfig(slashCommandConfig)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
