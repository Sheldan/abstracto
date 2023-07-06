package dev.sheldan.abstracto.twitch.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.twitch.config.TwitchFeatureDefinition;
import dev.sheldan.abstracto.twitch.config.TwitchSlashCommandNames;
import dev.sheldan.abstracto.twitch.model.template.ListTwitchStreamerResponseModel;
import dev.sheldan.abstracto.twitch.service.StreamerService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ListTwitchStreamer extends AbstractConditionableCommand {

    private static final String LIST_TWITCH_STREAMER_COMMAND = "listTwitchStreamer";
    private static final String LIST_TWITCH_STREAMER_RESPONSE = "listTwitchStreamer_response";

    @Autowired
    private StreamerService streamerService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        ListTwitchStreamerResponseModel model = streamerService.getStreamersFromServer(event.getGuild());
        return interactionService.replyEmbed(LIST_TWITCH_STREAMER_RESPONSE, model, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(TwitchSlashCommandNames.TWITCH)
                .groupName("streamer")
                .commandName("list")
                .build();

        return CommandConfiguration.builder()
                .name(LIST_TWITCH_STREAMER_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return TwitchFeatureDefinition.TWITCH;
    }
}
