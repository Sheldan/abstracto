package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.SlowModeService;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class SlowMode extends AbstractConditionableCommand {

    @Autowired
    private SlowModeService slowModeService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
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
            if(!channel.getGuild().equals(commandContext.getGuild())) {
                throw new EntityGuildMismatchException();
            }
        } else {
            if(commandContext.getChannel() instanceof TextChannel) {
                channel = (TextChannel) commandContext.getChannel();
            } else {
                throw new IllegalArgumentException("Not a text channel.");
            }
        }
        return slowModeService.setSlowMode(channel, duration)
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("duration").type(String.class).templated(true).build());
        parameters.add(Parameter.builder().name("channel").type(TextChannel.class).templated(true).optional(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).hasExample(true).build();
        return CommandConfiguration.builder()
                .name("slowmode")
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }
}
