package dev.sheldan.abstracto.core.commands.config;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.service.management.FeatureManagementService;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.exception.ConfigurationKeyNotFoundException;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ResetConfig extends AbstractConditionableCommand {

    @Autowired
    private ConfigService configService;

    @Autowired
    private DefaultConfigManagementService defaultConfigManagementService;

    @Autowired
    private FeatureManagementService featureManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Long serverId = commandContext.getGuild().getIdLong();
        if(!commandContext.getParameters().getParameters().isEmpty()) {
            String name = (String) commandContext.getParameters().getParameters().get(0);
            if(featureManagementService.featureExists(name)) {
                configService.resetConfigForFeature(name, serverId);
            } else if(defaultConfigManagementService.configKeyExists(name)) {
                configService.resetConfigForKey(name, serverId);
            } else {
                throw new ConfigurationKeyNotFoundException(name);
            }
        } else {
            configService.resetConfigForServer(serverId);
        }
        return CompletableFuture.completedFuture(CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter keyToChange = Parameter.builder().name("key").type(String.class).optional(true).templated(true).build();
        List<Parameter> parameters = Arrays.asList(keyToChange);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("resetConfig")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .templated(true)
                .async(true)
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
