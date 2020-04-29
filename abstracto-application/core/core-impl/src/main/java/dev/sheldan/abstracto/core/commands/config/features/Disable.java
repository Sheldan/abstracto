package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleInterface;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.models.template.commands.EnableModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Disable extends AbstractConditionableCommand {

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;


    @Override
    public CommandResult execute(CommandContext commandContext) {
        if(commandContext.getParameters().getParameters().isEmpty()) {
            EnableModel model = (EnableModel) ContextConverter.fromCommandContext(commandContext, EnableModel.class);
            model.setFeatures(featureFlagService.getAllFeatures());
            String response = templateService.renderTemplate("disable_features_response", model);
            channelService.sendTextInAChannel(response, commandContext.getChannel());
        } else {
            String flagKey = (String) commandContext.getParameters().getParameters().get(0);
            FeatureConfig feature = featureFlagService.getFeatureDisplayForFeature(flagKey);
            featureFlagService.disableFeature(feature, commandContext.getGuild().getIdLong());
            if(feature.getDependantFeatures() != null) {
                feature.getDependantFeatures().forEach(featureDisplay -> {
                    featureFlagService.disableFeature(featureDisplay, commandContext.getUserInitiatedContext().getServer());
                });
            }
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("featureName").type(String.class).optional(true).description("The feature to disable.").build();
        List<Parameter> parameters = Arrays.asList(featureName);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("disable")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .help(helpInfo)
                .templated(true)
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
