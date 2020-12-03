package dev.sheldan.abstracto.core.commands.config.features;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.commands.config.ConfigModuleInterface;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.AFeature;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModeDisplay;
import dev.sheldan.abstracto.core.models.template.commands.FeatureModesModel;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class FeatureModes extends AbstractConditionableCommand {

    public static final String FEATURE_MODES_RESPONSE_TEMPLATE_KEY = "feature_modes_response";
    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<FeatureModeDisplay> featureModes;
        if(commandContext.getParameters().getParameters().isEmpty()) {
            featureModes = featureModeService.getEffectiveFeatureModes(commandContext.getUserInitiatedContext().getServer());
        } else {
            String featureName = (String) commandContext.getParameters().getParameters().get(0);
            FeatureEnum featureEnum = featureConfigService.getFeatureEnum(featureName);
            AFeature feature = featureManagementService.getFeature(featureEnum.getKey());
            featureModes = featureModeService.getEffectiveFeatureModes(commandContext.getUserInitiatedContext().getServer(), feature);
        }
        FeatureModesModel model = FeatureModesModel.builder().featureModes(featureModes).build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInChannel(FEATURE_MODES_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("feature").type(String.class).optional(true).templated(true).build();
        List<Parameter> parameters = Arrays.asList(featureName);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("featureModes")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .templated(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .async(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
