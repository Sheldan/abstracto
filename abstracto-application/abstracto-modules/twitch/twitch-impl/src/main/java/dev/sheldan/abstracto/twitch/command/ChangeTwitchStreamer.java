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
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.twitch.config.TwitchFeatureDefinition;
import dev.sheldan.abstracto.twitch.config.TwitchSlashCommandNames;
import dev.sheldan.abstracto.twitch.exception.StreamerNotFoundInServerException;
import dev.sheldan.abstracto.twitch.model.database.Streamer;
import dev.sheldan.abstracto.twitch.service.StreamerService;
import dev.sheldan.abstracto.twitch.service.management.StreamerManagementService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Component
public class ChangeTwitchStreamer extends AbstractConditionableCommand {

    private static final String CHANGE_STREAMER_COMMAND = "changeTwitchStreamer";
    private static final String STREAMER_NAME_PARAMETER = "streamerName";
    private static final String STREAMER_NEW_VALUE_PARAMETER = "newValue";
    private static final String STREAMER_PROPERTY_PARAMETER = "property";
    private static final String CHANGE_TWITCH_STREAMER_RESPONSE = "changeTwitchStreamer_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private StreamerManagementService streamerManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private StreamerService streamerService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String streamerName = slashCommandParameterService.getCommandOption(STREAMER_NAME_PARAMETER, event, String.class);
        String property = slashCommandParameterService.getCommandOption(STREAMER_PROPERTY_PARAMETER, event, String.class);
        AServer server = serverManagementService.loadServer(event.getGuild());
        Streamer streamerInServerByName = streamerManagementService.getStreamerInServerByName(streamerName, server).orElseThrow(StreamerNotFoundInServerException::new);
        StreamerProperty propertyEnum = StreamerProperty.valueOf(property);
        String newValue = slashCommandParameterService.getCommandOption(STREAMER_NEW_VALUE_PARAMETER, event, String.class);
        updateStreamer(streamerInServerByName, propertyEnum, newValue);
        return interactionService.replyEmbed(CHANGE_TWITCH_STREAMER_RESPONSE, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private void updateStreamer(Streamer streamer, StreamerProperty propertyEnum, String newValue) {
        switch (propertyEnum) {
            case TARGET_CHANNEL -> {
                Matcher channelMatcher = Message.MentionType.CHANNEL.getPattern().matcher(newValue);
                if (channelMatcher.matches()) {
                    Long channelId = Long.parseLong(channelMatcher.group(1));
                    streamerService.changeStreamerNotificationToChannel(streamer, channelId);
                } else {
                    streamerService.changeStreamerNotificationToChannel(streamer, null);
                }
            }
            case STREAMER_MEMBER -> {
                Matcher memberMatcher = Message.MentionType.USER.getPattern().matcher(newValue);
                if (memberMatcher.matches()) {
                    Long channelId = Long.parseLong(memberMatcher.group(1));
                    streamerService.changeStreamerMemberToUserId(streamer, channelId);
                } else {
                    streamerService.changeStreamerMemberToUserId(streamer, null);
                }
            }
            case TEMPLATE_KEY -> {
                String newTemplateKey = newValue;
                if ("default".equals(newTemplateKey)) {
                    newTemplateKey = null;
                }
                streamerService.changeTemplateKeyTo(streamer, newTemplateKey);
            }
            case DISABLE_NOTIFICATIONS -> {
                Boolean newState = BooleanUtils.toBoolean(newValue);
                streamerService.disableNotificationsForStreamer(streamer, newState);
            }
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter streamerNameParameter = Parameter
                .builder()
                .templated(true)
                .name(STREAMER_NAME_PARAMETER)
                .type(String.class)
                .build();

        List<String> streamerProperties = Arrays
                .stream(StreamerProperty.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        Parameter streamerPropertyParameter = Parameter
                .builder()
                .templated(true)
                .name(STREAMER_PROPERTY_PARAMETER)
                .type(String.class)
                .choices(streamerProperties)
                .build();

        Parameter newValueParameter = Parameter
                .builder()
                .templated(true)
                .name(STREAMER_NEW_VALUE_PARAMETER)
                .type(String.class)
                .build();

        List<Parameter> parameters = Arrays.asList(streamerNameParameter, streamerPropertyParameter, newValueParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .rootCommandName(TwitchSlashCommandNames.TWITCH)
                .groupName("streamer")
                .commandName("edit")
                .build();

        return CommandConfiguration.builder()
                .name(CHANGE_STREAMER_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandOnly(true)
                .async(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return TwitchFeatureDefinition.TWITCH;
    }

    public enum StreamerProperty {
        TARGET_CHANNEL, STREAMER_MEMBER, DISABLE_NOTIFICATIONS, TEMPLATE_KEY
    }
}
