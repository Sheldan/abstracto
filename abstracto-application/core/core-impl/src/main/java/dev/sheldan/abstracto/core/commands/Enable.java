package dev.sheldan.abstracto.core.commands;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.config.AbstractoFeatures;
import dev.sheldan.abstracto.core.commands.utility.UtilityModuleInterface;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Enable implements Command {


    @Autowired
    private FeatureFlagService featureFlagManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String flagKey = (String) commandContext.getParameters().getParameters().get(0);
        featureFlagManagementService.enableFeature(flagKey, commandContext.getGuild().getIdLong());
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