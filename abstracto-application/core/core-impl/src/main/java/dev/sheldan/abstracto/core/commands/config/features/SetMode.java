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
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SetMode extends AbstractConditionableCommand {

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String featureName = (String) commandContext.getParameters().getParameters().get(0);
        String modeName = (String) commandContext.getParameters().getParameters().get(1);

        if(featureConfigService.doesFeatureExist(featureName)) {
            if(featureConfigService.isModeValid(featureName, modeName)) {
                featureModeService.setModeForFeatureTo(featureName, commandContext.getUserInitiatedContext().getServer(), modeName);
            } else {
                return CommandResult.fromError("Mode not available");
            }
        } else {
            return CommandResult.fromError("Feature does not exist");
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("feature").type(String.class).templated(true).build();
        Parameter newMode = Parameter.builder().name("newMode").type(String.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(featureName, newMode);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("setMode")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
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
