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
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.template.commands.EnableModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DisableFeature extends AbstractConditionableCommand {

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
            String response = templateService.renderTemplate("disable_features_response", model, commandContext.getGuild().getIdLong());
            return channelService.sendTextToChannel(response, commandContext.getChannel())
                    .thenApply(aVoid -> CommandResult.fromSuccess());
        } else {
            String flagKey = (String) commandContext.getParameters().getParameters().get(0);
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(flagKey);
            featureFlagService.disableFeature(feature, commandContext.getGuild().getIdLong());
            if(feature.getDependantFeatures() != null) {
                AServer server = serverManagementService.loadServer(commandContext.getGuild());
                feature.getDependantFeatures().forEach(featureDisplay ->
                    featureFlagService.disableFeature(featureDisplay, server)
                );
            }
            return CompletableFuture.completedFuture(CommandResult.fromSuccess());
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("featureName").templated(true).type(String.class).optional(true).build();
        List<Parameter> parameters = Arrays.asList(featureName);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("disableFeature")
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .async(true)
                .help(helpInfo)
                .templated(true)
                .supportsEmbedException(true)
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
