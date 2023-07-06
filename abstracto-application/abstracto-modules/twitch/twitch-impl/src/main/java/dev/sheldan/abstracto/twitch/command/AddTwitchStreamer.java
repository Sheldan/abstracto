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
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.twitch.config.TwitchFeatureDefinition;
import dev.sheldan.abstracto.twitch.config.TwitchSlashCommandNames;
import dev.sheldan.abstracto.twitch.service.StreamerService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class AddTwitchStreamer extends AbstractConditionableCommand {

    private static final String ADD_TWITCH_STREAMER_COMMAND = "addTwitchStreamer";
    private static final String STREAMER_NAME_PARAMETER = "streamerName";
    private static final String TARGET_CHANNEL_PARAMETER = "targetChannel";
    private static final String SERVER_MEMBER_PARAMETER = "streamerMember";
    private static final String ADD_TWITCH_STREAMER_RESPONSE = "addTwitchStreamer_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private StreamerService streamerService;

    @Autowired
    private AddTwitchStreamer self;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        // its likely that this command times out
        return event.deferReply().submit().thenCompose(interactionHook -> self.executeAddStreamerCommandLogic(event, interactionHook));

    }

    @Transactional
    public CompletableFuture<CommandResult> executeAddStreamerCommandLogic(SlashCommandInteractionEvent event, InteractionHook interactionHook) {
        String streamerName = slashCommandParameterService.getCommandOption(STREAMER_NAME_PARAMETER, event, String.class);
        GuildMessageChannel guildMessageChannel = null;
        if(slashCommandParameterService.hasCommandOption(TARGET_CHANNEL_PARAMETER, event)) {
            guildMessageChannel = slashCommandParameterService.getCommandOption(TARGET_CHANNEL_PARAMETER, event, TextChannel.class, GuildMessageChannel.class);
        }
        Member streamerMember = null;
        if(slashCommandParameterService.hasCommandOption(SERVER_MEMBER_PARAMETER, event)) {
            streamerMember = slashCommandParameterService.getCommandOption(SERVER_MEMBER_PARAMETER, event, Member.class);
        }
        streamerService.createStreamer(streamerName, guildMessageChannel, event.getMember(), streamerMember);
        return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(ADD_TWITCH_STREAMER_RESPONSE, new Object(), interactionHook))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter streamerNameParameter = Parameter
                .builder()
                .name(STREAMER_NAME_PARAMETER)
                .type(String.class)
                .templated(true)
                .build();

        Parameter targetChannelParameter = Parameter
                .builder()
                .name(TARGET_CHANNEL_PARAMETER)
                .type(TextChannel.class)
                .optional(true)
                .templated(true)
                .build();

        Parameter streamerMemberParameter = Parameter
                .builder()
                .name(SERVER_MEMBER_PARAMETER)
                .type(Member.class)
                .optional(true)
                .templated(true)
                .build();

        List<Parameter> parameters = Arrays.asList(streamerNameParameter, targetChannelParameter, streamerMemberParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(TwitchSlashCommandNames.TWITCH)
                .groupName("streamer")
                .commandName("add")
                .build();

        return CommandConfiguration.builder()
                .name(ADD_TWITCH_STREAMER_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
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
}
