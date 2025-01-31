package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.AbstractoTemplatedException;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticSlashCommandNames;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingMode;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.exception.TrackedEmoteNotFoundException;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This command is used to show the image and provide the link of an external {@link TrackedEmote}. It is only available, if the
 * EmoteTrackingMode.EXTERNAL_EMOTES is active.
 */
@Component
public class ShowExternalTrackedEmote extends AbstractConditionableCommand {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    public static final String SHOW_EXTERNAL_TRACKED_EMOTE_RESPONSE_TEMPLATE_KEY = "showExternalTrackedEmote_response";
    private static final String SHOW_EXTERNAL_TRACKED_EMOTE_COMMAND_NAME = "showExternalTrackedEmote";
    private static final String SHOW_EXTERNAL_TRACKED_EMOTE_TRACKED_EMOTE = "trackedEmote";

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String emote = slashCommandParameterService.getCommandOption(SHOW_EXTERNAL_TRACKED_EMOTE_TRACKED_EMOTE, event, String.class);
        Emoji emoji = slashCommandParameterService.loadEmoteFromString(emote, event.getGuild());
        if(emoji instanceof CustomEmoji) {
            Long emoteId = ((CustomEmoji) emoji).getIdLong();
            return createResponse(event, emoteId);
        } else if(StringUtils.isNumeric(emote)) {
            return createResponse(event, Long.parseLong(emote));
        } else {
            throw new TrackedEmoteNotFoundException();
        }
    }

    private CompletableFuture<CommandResult> createResponse(SlashCommandInteractionEvent event, Long emoteId) {
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByTrackedEmoteServer(new ServerSpecificId(event.getGuild().getIdLong(), emoteId));
        if(!trackedEmote.getExternal()) {
            throw new AbstractoTemplatedException("Emote is not external", "showExternalTrackedEmote_emote_is_not_external");
        }
        return interactionService.replyEmbed(SHOW_EXTERNAL_TRACKED_EMOTE_RESPONSE_TEMPLATE_KEY, trackedEmote, event)
            .thenApply(unused -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter trackedEmoteParameter = Parameter
                .builder()
                .name(SHOW_EXTERNAL_TRACKED_EMOTE_TRACKED_EMOTE)
                .templated(true)
                .type(TrackedEmote.class)
                .build();
        parameters.add(trackedEmoteParameter);

        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(StatisticSlashCommandNames.STATISTIC)
            .groupName("show")
            .commandName("externaltrackedemote")
            .build();

        return CommandConfiguration.builder()
                .name(SHOW_EXTERNAL_TRACKED_EMOTE_COMMAND_NAME)
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .slashCommandOnly(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return StatisticFeatureDefinition.EMOTE_TRACKING;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(EmoteTrackingMode.EXTERNAL_EMOTES);
    }
}
