package dev.sheldan.abstracto.core.commands.config;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.ConfigurationKeyNotFoundException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ResetConfig extends AbstractConditionableCommand {

    @Autowired
    private ConfigService configService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    private static final String RESPONSE_TEMPLATE = "resetConfig_response";
    private static final String KEY_PARAMETER = "key";
    private static final String RESET_CONFIG_COMMAND = "resetConfig";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        long serverId = event.getGuild().getIdLong();
        if(!slashCommandParameterService.hasCommandOption("key", event)) {
            configService.resetConfigForServer(serverId);
        } else {
            String key = slashCommandParameterService.getCommandOption(KEY_PARAMETER, event, String.class);
            if(featureManagementService.featureExists(key)) {
                configService.resetConfigForFeature(key, serverId);
            } else if(defaultConfigManagementService.configKeyExists(key)) {
                configService.resetConfigForKey(key, serverId);
            } else {
                throw new ConfigurationKeyNotFoundException(key);
            }
        }
        return interactionService.replyEmbed(RESPONSE_TEMPLATE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter keyToChange = Parameter
                .builder()
                .name("key")
                .type(String.class)
                .optional(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(keyToChange);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .rootCommandName(CoreSlashCommandNames.CONFIG)
                .commandName("reset")
                .build();

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name(RESET_CONFIG_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
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
        return CoreFeatureDefinition.CORE_FEATURE;
    }

}
