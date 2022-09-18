package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.SlowModeService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class SlowMode extends AbstractConditionableCommand {

    private static final String CHANNEL_PARAMETER = "channel";
    private static final String DURATION_PARAMETER = "duration";
    private static final String SLOWMODE_COMMAND = "slowmode";
    private static final String SLOWMODE_RESPONSE = "slowmode_response";

    @Autowired
    private SlowModeService slowModeService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

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
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        TextChannel channel;
        String durationString = slashCommandParameterService.getCommandOption(DURATION_PARAMETER, event, String.class);
        Duration duration;
        if(durationString.equalsIgnoreCase("off")) {
            duration = Duration.ZERO;
        } else {
            duration = ParseUtils.parseDuration(durationString);
        }
        if(slashCommandParameterService.hasCommandOption(CHANNEL_PARAMETER, event)) {
            channel = slashCommandParameterService.getCommandOption(CHANNEL_PARAMETER, event, TextChannel.class);
            if(!channel.getGuild().equals(event.getGuild())) {
                throw new EntityGuildMismatchException();
            }
        } else {
            if(event.getChannel() instanceof TextChannel) {
                channel = (TextChannel) event.getChannel();
            } else {
                throw new IllegalArgumentException("Not a text channel.");
            }
        }
        return slowModeService.setSlowMode(channel, duration)
                .thenCompose(unused -> interactionService.replyEmbed(SLOWMODE_RESPONSE, event))
                .thenApply(aVoid -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter durationParameter = Parameter
                .builder()
                .name(DURATION_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();
        Parameter channelParameter = Parameter
                .builder()
                .name(CHANNEL_PARAMETER)
                .type(TextChannel.class)
                .templated(true)
                .optional(true)
                .build();
        List<Parameter> parameters = Arrays.asList(durationParameter, channelParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .hasExample(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.MODERATION)
                .commandName(SLOWMODE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(SLOWMODE_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
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
