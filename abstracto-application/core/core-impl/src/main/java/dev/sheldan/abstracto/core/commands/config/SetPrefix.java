package dev.sheldan.abstracto.core.commands.config;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.ParameterValidator;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.command.config.validator.MaxStringLengthValidator;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SetPrefix extends AbstractConditionableCommand {

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
        List<ParameterValidator> validators = Arrays.asList(MaxStringLengthValidator.max(10L));
        Parameter newPrefixParameter = Parameter.builder().name("prefix").validators(validators).type(String.class).templated(true).build();
        List<Parameter> parameters = Arrays.asList(newPrefixParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("setPrefix")
                .module(ConfigModuleDefinition.CONFIG)
                .parameters(parameters)
                .supportsEmbedException(true)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
