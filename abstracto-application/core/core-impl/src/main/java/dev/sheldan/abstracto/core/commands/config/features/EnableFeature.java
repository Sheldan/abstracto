package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleInterface;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.FeatureValidationResult;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.EnableModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        if(commandContext.getParameters().getParameters().isEmpty()) {
            EnableModel model = (EnableModel) ContextConverter.fromCommandContext(commandContext, EnableModel.class);
            model.setFeatures(featureConfigService.getAllFeatures());
            String response = templateService.renderTemplate("enable_features_response", model, commandContext.getGuild().getIdLong());
            return channelService.sendTextToChannel(response, commandContext.getChannel())
                    .thenApply(message -> CommandResult.fromSuccess());
        } else {
            String flagKey = (String) commandContext.getParameters().getParameters().get(0);
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(flagKey);
            AServer server = serverManagementService.loadServer(commandContext.getUserInitiatedContext().getGuild().getIdLong());
            FeatureValidationResult featureSetup = featureConfigService.validateFeatureSetup(feature, server);
            if(Boolean.FALSE.equals(featureSetup.getValidationResult())) {
                log.info("Feature {} has failed the setup validation. Notifying user.", flagKey);
                channelService.sendTextToChannelNotAsync(templateService.renderTemplatable(featureSetup, commandContext.getGuild().getIdLong()),
                        commandContext.getChannel());
            }
            featureFlagService.enableFeature(feature, server);
            if(feature.getRequiredFeatures() != null) {
                feature.getRequiredFeatures().forEach(featureDisplay -> {
                    log.info("Also enabling required feature {}.", featureDisplay.getFeature().getKey());
                    featureFlagService.enableFeature(featureDisplay, server);
                });
            }
            return CompletableFuture.completedFuture(CommandResult.fromSuccess());
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("featureName").type(String.class).optional(true).templated(true).build();
        List<Parameter> parameters = Arrays.asList(featureName);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("enableFeature")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .async(true)
                .supportsEmbedException(true)
                .templated(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.remove(featureEnabledCondition);
        return conditions;
    }
}
