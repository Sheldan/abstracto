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
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.management.FeatureModeManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class EnableMode extends AbstractConditionableCommand {

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private FeatureModeManagementService featureModeManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String featureName = (String) commandContext.getParameters().getParameters().get(0);
        String modeName = (String) commandContext.getParameters().getParameters().get(1);
        FeatureEnum featureEnum = featureConfigService.getFeatureEnum(featureName);
        FeatureMode featureMode = featureModeService.getFeatureModeForKey(modeName);
        AServer server = serverManagementService.loadServer(commandContext.getGuild().getIdLong());
        featureModeService.enableFeatureModeForFeature(featureEnum, server, featureMode);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("feature").type(String.class).templated(true).build();
        Parameter mode = Parameter.builder().name("mode").type(String.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(featureName, mode);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("enableMode")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .templated(true)
                .supportsEmbedException(true)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
