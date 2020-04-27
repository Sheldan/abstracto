package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModule;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.service.SlowModeService;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class SlowMode extends AbstractConditionableCommand {

    @Autowired
    private SlowModeService slowModeService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        TextChannel channel;
        String durationString = (String) commandContext.getParameters().getParameters().get(0);
        Duration duration;
        if(durationString.equalsIgnoreCase("off")) {
            duration = Duration.ZERO;
        } else {
            duration = ParseUtils.parseDuration(durationString);
        }
        if(commandContext.getParameters().getParameters().size() == 2) {
            channel = (TextChannel) commandContext.getParameters().getParameters().get(1);
        } else {
            channel = commandContext.getChannel();
        }
        slowModeService.setSlowMode(channel, duration);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("duration").type(String.class).optional(false).build());
        parameters.add(Parameter.builder().name("channel").type(TextChannel.class).optional(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("slowmode")
                .module(ModerationModule.MODERATION)
                .templated(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.MODERATION;
    }
}
