package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.RoleImmunityService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class MakeImmune extends AbstractConditionableCommand {

    private static final String ROLE_PARAMETER = "role";
    private static final String EFFECT_PARAMETER = "effect";
    private static final String MAKE_IMMUNE_COMMAND = "makeImmune";
    private static final String MAKE_IMMUNE_RESPONSE = "makeImmune_response";

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
        if(!role.getGuild().equals(commandContext.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        roleImmunityService.makeRoleImmune(role, name);
        return CommandResult.fromSuccess();
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String name = slashCommandParameterService.getCommandOption(EFFECT_PARAMETER, event, String.class);
        Role role = slashCommandParameterService.getCommandOption(ROLE_PARAMETER, event, Role.class);

        if(!role.getGuild().equals(event.getGuild())) {
            throw new EntityGuildMismatchException();
        }
        roleImmunityService.makeRoleImmune(role, name);
        return interactionService.replyEmbed(MAKE_IMMUNE_RESPONSE, event)
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
                .commandName(MAKE_IMMUNE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(MAKE_IMMUNE_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .templated(true)
                .supportsEmbedException(true)
                .slashCommandConfig(slashCommandConfig)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
