package dev.sheldan.abstracto.core.commands.config;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class SetConfig extends AbstractConditionableCommand {

    private static final String KEY_PARAMETER = "key";
    private static final String VALUE_PARAMETER = "value";
    private static final String SET_CONFIG_COMMAND = "setConfig";
    @Autowired
    private ConfigService configService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    private static final String RESPONSE_TEMPLATE = "setConfig_response";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String key = slashCommandParameterService.getCommandOption(KEY_PARAMETER, event, String.class);
        String value = slashCommandParameterService.getCommandOption(VALUE_PARAMETER, event, String.class);
        configService.setOrCreateConfigValue(key, event.getGuild().getIdLong(), value);
        return interactionService.replyEmbed(RESPONSE_TEMPLATE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter keyToChange = Parameter
                .builder()
                .name(KEY_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        Parameter valueToSet = Parameter
                .builder()
                .name(VALUE_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(keyToChange, valueToSet);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .rootCommandName(CoreSlashCommandNames.CONFIG)
                .commandName("set")
                .build();

        return CommandConfiguration.builder()
                .name(SET_CONFIG_COMMAND)
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
