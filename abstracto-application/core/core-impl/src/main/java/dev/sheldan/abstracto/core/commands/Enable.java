package dev.sheldan.abstracto.core.commands;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.Parameter;
import dev.sheldan.abstracto.config.AbstractoFeatures;
import dev.sheldan.abstracto.core.commands.utility.UtilityModuleInterface;
import dev.sheldan.abstracto.core.service.management.FeatureFlagManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Enable implements Command {


    @Autowired
    private FeatureFlagManagementService featureFlagManagementService;
    @Override
    public CommandResult execute(CommandContext commandContext) {
        String flagKey = (String) commandContext.getParameters().getParameters().get(0);
        featureFlagManagementService.updateOrCreateFeatureFlag(flagKey, commandContext.getGuild().getIdLong(), true);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter featureName = Parameter.builder().name("featureName").type(String.class).description("The feature to enable.").build();
        List<Parameter> parameters = Arrays.asList(featureName);
        return CommandConfiguration.builder()
                .name("enable")
                .module(UtilityModuleInterface.UTILITY)
                .parameters(parameters)
                .description("Enables features for this server.")
                .causesReaction(true)
                .build();
    }

    @Override
    public String getFeature() {
        return AbstractoFeatures.CORE;
    }
}
