package dev.sheldan.abstracto.core.commands.config;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.interactive.InteractiveService;
import dev.sheldan.abstracto.core.models.AServerChannelUserId;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.SetupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Setup extends AbstractConditionableCommand {

    @Autowired
    private InteractiveService interactiveService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private SetupService setupService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String name = (String) commandContext.getParameters().getParameters().get(0);
        if(featureManagementService.featureExists(name)) {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(name);
            AServerChannelUserId initiatingUser = AServerChannelUserId
                    .builder()
                    .guildId(commandContext.getGuild().getIdLong())
                    .channelId(commandContext.getChannel().getIdLong())
                    .userId(commandContext.getAuthor().getIdLong())
                    .build();
            setupService.performSetup(feature, initiatingUser, commandContext.getMessage().getIdLong());
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter newPrefixParameter = Parameter.builder().name("feature").type(String.class).build();
        List<Parameter> parameters = Arrays.asList(newPrefixParameter);
        HelpInfo helpInfo = HelpInfo.builder().build();
        return CommandConfiguration.builder()
                .name("setup")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .help(helpInfo)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}