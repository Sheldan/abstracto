package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.FeatureSwitchModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
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
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ServerManagementService serverManagementService;

    private static final String ENABLE_FEATURE_DEPENDENCIES_RESPONSE_TEMPLATE_KEY = "enableFeature_feature_dependencies_response";
    private static final String ENABLE_FEATURE_ALL_FEATURES_RESPONSE = "enableFeature_all_features_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        if(commandContext.getParameters().getParameters().isEmpty()) {
            FeatureSwitchModel model = (FeatureSwitchModel) ContextConverter.fromCommandContext(commandContext, FeatureSwitchModel.class);
            model.setFeatures(featureConfigService.getAllFeatures());
            MessageToSend messageToSend = templateService.renderEmbedTemplate(ENABLE_FEATURE_ALL_FEATURES_RESPONSE, model, commandContext.getGuild().getIdLong());
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                    .thenApply(message -> CommandResult.fromIgnored());
        } else {
            String featureKey = (String) commandContext.getParameters().getParameters().get(0);
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(featureKey);
            AServer server = serverManagementService.loadServer(commandContext.getUserInitiatedContext().getGuild().getIdLong());
            FeatureValidationResult featureSetup = featureConfigService.validateFeatureSetup(feature, server);
            if(Boolean.FALSE.equals(featureSetup.getValidationResult())) {
                log.info("Feature {} has failed the setup validation. Notifying user.", featureKey);
                channelService.sendTextToChannelNotAsync(templateService.renderTemplatable(featureSetup, commandContext.getGuild().getIdLong()),
                        commandContext.getChannel());
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
            if(featureDependencies.isEmpty()) {
                return CompletableFuture.completedFuture(CommandResult.fromSuccess());
            } else {
                List<String> additionalFeatures = featureDependencies
                        .stream()
                        .map(featureDef -> featureDef.getFeature().getKey()).
                                collect(Collectors.toList());
                FeatureSwitchModel model = (FeatureSwitchModel) ContextConverter.fromCommandContext(commandContext, FeatureSwitchModel.class);
                model.setFeatures(additionalFeatures);
                MessageToSend messageToSend = templateService.renderEmbedTemplate(ENABLE_FEATURE_DEPENDENCIES_RESPONSE_TEMPLATE_KEY,
                        model, commandContext.getGuild().getIdLong());
                return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                        .thenApply(message -> CommandResult.fromIgnored());
            }
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("featureName").type(String.class).optional(true).templated(true).build();
        List<Parameter> parameters = Arrays.asList(featureName);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("enableFeature")
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .async(true)
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
}
