package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.experience.config.features.ExperienceFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Command used to change the experience multiplier on the server.
 */
@Component
@Slf4j
public class ExpScale extends AbstractConditionableCommand {

    public static final String EXP_MULTIPLIER_KEY = "expMultiplier";

    @Autowired
    private ConfigService configService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Double scale = (Double) commandContext.getParameters().getParameters().get(0);
        Long guildId = commandContext.getGuild().getIdLong();
        configService.setDoubleValue(EXP_MULTIPLIER_KEY, guildId, scale);
        log.info("Setting experience scale to {} for {}", scale, guildId);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("scale").templated(true).type(Double.class).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("expScale")
                .module(ExperienceModule.EXPERIENCE)
                .causesReaction(true)
                .templated(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ExperienceFeature.EXPERIENCE;
    }
}
