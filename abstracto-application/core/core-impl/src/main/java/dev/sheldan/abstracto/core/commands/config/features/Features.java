package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.converter.FeatureFlagConverter;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.property.FeatureFlagProperty;
import dev.sheldan.abstracto.core.models.template.commands.FeaturesModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.DefaultFeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class Features extends AbstractConditionableCommand {

    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private FeatureFlagConverter featureFlagConverter;

    @Autowired
    private DefaultFeatureFlagManagementService defaultFeatureFlagManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        AServer server = serverManagementService.loadServer(commandContext.getGuild());
        List<AFeatureFlag> features = featureFlagManagementService.getFeatureFlagsOfServer(server);
        List<FeatureFlagProperty> defaultFeatureFlagProperties = defaultFeatureFlagManagementService.getAllDefaultFeatureFlags();
        FeaturesModel featuresModel = (FeaturesModel) ContextConverter.fromCommandContext(commandContext, FeaturesModel.class);
        features.sort(Comparator.comparing(o -> o.getFeature().getKey()));
        featuresModel.setFeatures(featureFlagConverter.fromFeatureFlags(features));
        defaultFeatureFlagProperties = defaultFeatureFlagProperties
                .stream()
                .filter(featureFlagProperty ->
                        features
                                .stream()
                                .noneMatch(aFeatureFlag ->
                                        aFeatureFlag.getFeature().getKey()
                                                .equals(featureFlagProperty.getFeatureName())))
                .collect(Collectors.toList());
        defaultFeatureFlagProperties.sort(Comparator.comparing(FeatureFlagProperty::getFeatureName));
        featuresModel.setDefaultFeatures(featureFlagConverter.fromFeatureFlagProperties(defaultFeatureFlagProperties));
        MessageToSend messageToSend = templateService.renderEmbedTemplate("features_response", featuresModel, commandContext.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("features")
                .module(ConfigModuleDefinition.CONFIG)
                .templated(true)
                .async(true)
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
