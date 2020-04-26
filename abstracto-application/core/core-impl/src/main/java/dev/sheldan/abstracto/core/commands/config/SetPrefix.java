package dev.sheldan.abstracto.core.commands.config;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SetPrefix implements Command {

    @Autowired
    private ConfigService configService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String prefixValue = (String) commandContext.getParameters().getParameters().get(0);
        configService.setStringValue("prefix", commandContext.getGuild().getIdLong(), prefixValue);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter newPrefixParameter = Parameter.builder().name("prefix").type(String.class).description("The new prefix to be used for this server.").build();
        List<Parameter> parameters = Arrays.asList(newPrefixParameter);
        return CommandConfiguration.builder()
                .name("setPrefix")
                .module(ConfigModuleInterface.CONFIG)
                .parameters(parameters)
                .description("Used to change the prefix on this server.")
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
