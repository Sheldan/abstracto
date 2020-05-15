package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleInterface;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.converter.FeatureFlagConverter;
import dev.sheldan.abstracto.core.models.database.AFeatureFlag;
import dev.sheldan.abstracto.core.models.template.commands.FeaturesModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<AFeatureFlag> features = featureFlagManagementService.getFeatureFlagsOfServer(commandContext.getUserInitiatedContext().getServer());
        FeaturesModel featuresModel = (FeaturesModel) ContextConverter.fromCommandContext(commandContext, FeaturesModel.class);
        featuresModel.setFeatures(featureFlagConverter.fromFeatureFlags(features));
        MessageToSend messageToSend = templateService.renderEmbedTemplate("features_response", featuresModel);
        channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("features")
                .module(ConfigModuleInterface.CONFIG)
                .templated(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
