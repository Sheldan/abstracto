package dev.sheldan.abstracto.experience.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.experience.config.ExperienceFeatures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExpScale extends AbstractConditionableCommand {

    public static final String EXP_MULTIPLIER_KEY = "expMultiplier";
    @Autowired
    private ConfigService configService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        Double scale = (Double) commandContext.getParameters().getParameters().get(0);
        configService.setDoubleValue(EXP_MULTIPLIER_KEY, commandContext.getGuild().getIdLong(), scale);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("scale").type(Double.class).build());
        HelpInfo helpInfo = HelpInfo.builder().longHelp("The new scale of experience on this server.").usage("expScale").build();
        return CommandConfiguration.builder()
                .name("expScale")
                .module(ExperienceModule.EXPERIENCE)
                .description("Sets the experience scale of this server to this value.")
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public String getFeature() {
        return ExperienceFeatures.EXPERIENCE;
    }
}
