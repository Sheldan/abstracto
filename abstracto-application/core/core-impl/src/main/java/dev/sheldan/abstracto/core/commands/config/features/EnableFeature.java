package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.interaction.slash.CoreSlashCommandNames;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.FeatureSwitchModel;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class EnableFeature extends AbstractConditionableCommand {

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    private static final String ENABLE_FEATURE_DEPENDENCIES_RESPONSE_TEMPLATE_KEY = "enableFeature_feature_dependencies_response";
    private static final String ENABLE_FEATURE_RESPONSE_TEMPLATE_KEY = "enableFeature_response";
    private static final String FEATURE_NAME_PARAMETER = "featureName";
    private static final String ENABLE_FEATURE_COMMAND = "enableFeature";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String featureName = slashCommandParameterService.getCommandOption(FEATURE_NAME_PARAMETER, event, String.class);
        EnableFeatureResult enableFeatureResult = enableFeature(event.getGuild().getIdLong(), featureName);
        if(enableFeatureResult.featureDependencies.isEmpty()) {
            FeatureSwitchModel model = FeatureSwitchModel
                    .builder()
                    .validationText(enableFeatureResult.validationResult.getValidationText())
                    .build();
            return interactionService.replyEmbed(ENABLE_FEATURE_RESPONSE_TEMPLATE_KEY, model, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        } else {
            List<String> additionalFeatures = enableFeatureResult.featureDependencies
                    .stream()
                    .map(featureDef -> featureDef.getFeature().getKey()).
                    collect(Collectors.toList());
            FeatureSwitchModel model = FeatureSwitchModel
                    .builder()
                    .validationText(enableFeatureResult.validationResult.getValidationText())
                    .features(additionalFeatures)
                    .build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate(ENABLE_FEATURE_DEPENDENCIES_RESPONSE_TEMPLATE_KEY,
                    model, event.getGuild().getIdLong());
            return interactionService.replyMessageToSend(messageToSend, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        }
    }

    private EnableFeatureResult enableFeature(Long serverId, String featureKey) {
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(featureKey);
        AServer server = serverManagementService.loadServer(serverId);
        FeatureValidationResult validationResult = featureConfigService.validateFeatureSetup(feature, server);
        if(Boolean.FALSE.equals(validationResult.getValidationResult())) {
            log.info("Feature {} has failed the setup validation. Notifying user.", featureKey);
            validationResult.setValidationText(templateService.renderTemplatable(validationResult, serverId));
        }
        featureFlagService.enableFeature(feature, server);
        List<FeatureConfig> featureDependencies = new ArrayList<>();
        if(feature.getRequiredFeatures() != null) {
            feature.getRequiredFeatures().forEach(featureDisplay -> {
                log.info("Also enabling required feature {}.", featureDisplay.getFeature().getKey());
                if(!featureFlagService.isFeatureEnabled(featureDisplay, server)) {
                    featureFlagService.enableFeature(featureDisplay, server);
                    featureDependencies.add(featureDisplay);
                }
            });
        }
        EnableFeatureResult result = new EnableFeatureResult();
        result.featureDependencies = featureDependencies;
        result.validationResult = validationResult;
        return result;
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), FEATURE_NAME_PARAMETER)) {
            String input = event.getFocusedOption().getValue().toLowerCase();
            return featureConfigService.getAllFeatures()
                .stream()
                .map(String::toLowerCase)
                .filter(lowerCase -> lowerCase.startsWith(input))
                .toList();
        }
        return new ArrayList<>();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter
                .builder()
                .name(FEATURE_NAME_PARAMETER)
                .type(String.class)
                .supportsAutoComplete(true)
                .templated(true)
                .build();
        List<Parameter> parameters = Arrays.asList(featureName);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .rootCommandName(CoreSlashCommandNames.FEATURE)
                .commandName("enable")
                .build();

        return CommandConfiguration.builder()
                .name(ENABLE_FEATURE_COMMAND)
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .async(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .templated(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.remove(featureEnabledCondition);
        return conditions;
    }

    private static class EnableFeatureResult {
        private FeatureValidationResult validationResult;
        private List<FeatureConfig> featureDependencies;
    }
}
