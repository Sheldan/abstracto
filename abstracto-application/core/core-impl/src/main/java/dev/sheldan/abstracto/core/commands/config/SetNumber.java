package dev.sheldan.abstracto.core.commands.config;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.commands.channels.ChannelsModuleInterface;
import dev.sheldan.abstracto.core.config.AbstractoFeatures;
import dev.sheldan.abstracto.core.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SetNumber implements Command {

    @Autowired
    private ConfigService configService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        String key = (String) commandContext.getParameters().getParameters().get(0);
        Double value = (Double) commandContext.getParameters().getParameters().get(1);
        configService.setDoubleValue(key, commandContext.getGuild().getIdLong(), value);

        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter channelGroupName = Parameter.builder().name("key").type(String.class).description("The key to change.").build();
        Parameter channelToAdd = Parameter.builder().name("value").type(Double.class).description("The numeric value to use for the config.").build();
        List<Parameter> parameters = Arrays.asList(channelGroupName, channelToAdd);
        return CommandConfiguration.builder()
                .name("setNumber")
                .module(ChannelsModuleInterface.CHANNELS)
                .parameters(parameters)
                .description("Used to change the config on this server.")
                .causesReaction(true)
                .build();
    }

    @Override
    public String getFeature() {
        return AbstractoFeatures.CORE;
    }
}
