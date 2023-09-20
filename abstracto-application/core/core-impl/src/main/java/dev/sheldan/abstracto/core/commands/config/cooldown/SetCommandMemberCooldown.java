package dev.sheldan.abstracto.core.commands.config.cooldown;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class SetCommandMemberCooldown extends AbstractConditionableCommand {

    private static final String SET_COMMAND_MEMBER_COOLDOWN = "setCommandMemberCooldown";
    private static final String COMMAND_NAME_PARAMETER = "commandName";
    private static final String COOLDOWN_DURATION_PARAMETER = "duration";
    private static final String SET_COOLDOWN_MEMBER_RESPONSE_TEMPLATE_KEY = "setCommandMemberCooldown_response";

    @Autowired
    private CommandService commandService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String commandName = slashCommandParameterService.getCommandOption(COMMAND_NAME_PARAMETER, event, String.class);
        String cooldownDurationString = slashCommandParameterService.getCommandOption(COOLDOWN_DURATION_PARAMETER, event, Duration.class, String.class);
        Duration duration = ParseUtils.parseDuration(cooldownDurationString);
        commandService.setServerCooldownTo(commandName, event.getGuild(), duration);
        return interactionService.replyEmbed(SET_COOLDOWN_MEMBER_RESPONSE_TEMPLATE_KEY, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter commandName = Parameter
                .builder()
                .name(COMMAND_NAME_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        Parameter cooldownDuration = Parameter
                .builder()
                .name(COOLDOWN_DURATION_PARAMETER)
                .type(Duration.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(commandName, cooldownDuration);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(CoreSlashCommandNames.COOLDOWN)
                .groupName("commandMember")
                .commandName("set")
                .build();

        return CommandConfiguration.builder()
                .name(SET_COMMAND_MEMBER_COOLDOWN)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .templated(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
