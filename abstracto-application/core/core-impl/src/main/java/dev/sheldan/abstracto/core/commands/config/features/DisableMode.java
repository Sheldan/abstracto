package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AFeatureMode;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureModeManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import java.util.ArrayList;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DisableMode extends AbstractConditionableCommand {

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;

    @Autowired
    private FeatureModeManagementService featureModeManagementService;

    @Autowired
    private FeatureManagementService featureManagementService;

    private static final String DISABLE_MODE_RESPONSE_KEY = "disableMode_response";
    private static final String FEATURE_PARAMETER = "feature";
    private static final String MODE_PARAMETER = "mode";
    private static final String DISABLE_MODE_COMMAND = "disableMode";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String featureName = slashCommandParameterService.getCommandOption(FEATURE_PARAMETER, event, String.class);
        String modeName = slashCommandParameterService.getCommandOption(MODE_PARAMETER, event, String.class);
        FeatureDefinition featureDefinition = featureConfigService.getFeatureEnum(featureName);
        FeatureMode featureMode = featureModeService.getFeatureModeForKey(featureName, modeName);
        AServer server = serverManagementService.loadServer(event.getGuild());
        featureModeService.disableFeatureModeForFeature(featureDefinition, server, featureMode);
        return interactionService.replyEmbed(DISABLE_MODE_RESPONSE_KEY, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        String input = event.getFocusedOption().getValue().toLowerCase();
        AServer server = serverManagementService.loadServer(event.getGuild());
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), FEATURE_PARAMETER)) {
            return featureFlagManagementService.getFeatureFlagsOfServer(server)
                .stream()
                .filter(AFeatureFlag::isEnabled)
                .map(aFeatureFlag -> aFeatureFlag.getFeature().getKey().toLowerCase())
                .filter(featureName ->  featureName.toLowerCase().startsWith(input))
                .toList();
        } else if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), MODE_PARAMETER)) {
            String featureName = slashCommandParameterService.getCommandOption(FEATURE_PARAMETER, event, String.class);
            if(featureName.isBlank()) {
                return new ArrayList<>();
            }
            FeatureDefinition featureDefinition = featureConfigService.getFeatureEnum(featureName);
            AFeature feature = featureManagementService.getFeature(featureDefinition.getKey());
            List<AFeatureMode> modes = featureModeManagementService.getFeatureModesOfFeatureInServer(server, feature);
            return modes
                .stream()
                .map(mode -> mode.getFeatureMode().toLowerCase())
                .filter(string -> string.startsWith(input))
                .toList();
        }
        return new ArrayList<>();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter
                .builder()
                .name(FEATURE_PARAMETER)
                .type(String.class)
                .supportsAutoComplete(true)
                .templated(true)
                .build();

        Parameter mode = Parameter
                .builder()
                .name(MODE_PARAMETER)
                .type(String.class)
                .supportsAutoComplete(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(featureName, mode);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .rootCommandName(CoreSlashCommandNames.FEATURE)
                .commandName(DISABLE_MODE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(DISABLE_MODE_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .templated(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
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
