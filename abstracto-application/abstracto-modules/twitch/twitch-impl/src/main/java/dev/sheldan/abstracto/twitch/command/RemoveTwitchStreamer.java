package dev.sheldan.abstracto.twitch.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.twitch.config.TwitchFeatureDefinition;
import dev.sheldan.abstracto.twitch.config.TwitchSlashCommandNames;
import dev.sheldan.abstracto.twitch.service.StreamerService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class RemoveTwitchStreamer extends AbstractConditionableCommand {

    private static final String REMOVE_TWITCH_STREAMER_COMMAND = "removeTwitchStreamer";
    private static final String STREAMER_NAME_PARAMETER = "streamerName";
    private static final String REMOVE_TWITCH_STREAMER_RESPONSE = "removeTwitchStreamer_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private StreamerService streamerService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String streamerName = slashCommandParameterService.getCommandOption(STREAMER_NAME_PARAMETER, event, String.class);
        streamerService.removeStreamer(streamerName, event.getGuild());
        return interactionService.replyEmbed(REMOVE_TWITCH_STREAMER_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter streamerNameParameter = Parameter
                .builder()
                .name(STREAMER_NAME_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();

        List<Parameter> parameters = Arrays.asList(streamerNameParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(TwitchSlashCommandNames.TWITCH)
                .groupName("streamer")
                .commandName("remove")
                .build();

        return CommandConfiguration.builder()
                .name(REMOVE_TWITCH_STREAMER_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .slashCommandOnly(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return TwitchFeatureDefinition.TWITCH;
    }
}
