package dev.sheldan.abstracto.giveaway.command;

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
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.giveaway.config.GiveawayFeatureDefinition;
import dev.sheldan.abstracto.giveaway.config.GiveawaySlashCommandNames;
import dev.sheldan.abstracto.giveaway.model.GiveawayCreationRequest;
import dev.sheldan.abstracto.giveaway.service.GiveawayService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class GreateGiveaway extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "createGiveaway";
    private static final String TITLE_PARAMETER = "title";
    private static final String DESCRIPTION_PARAMETER = "description";
    private static final String BENEFACTOR_PARAMETER = "benefactor";
    private static final String CHANNEL_PARAMETER = "channel";
    private static final String DURATION_PARAMETER = "duration";
    private static final String WINNERS_PARAMETER = "winners";

    private static final String CREATE_GIVEAWAY_RESPONSE_TEMPLATE_KEY = "createGiveaway_response";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private GiveawayService giveawayService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        return event.deferReply()
                .submit()
                .thenCompose(interactionHook -> {
                    String title = slashCommandParameterService.getCommandOption(TITLE_PARAMETER, event, String.class);
                    String description;
                    if(slashCommandParameterService.hasCommandOption(DESCRIPTION_PARAMETER, event)) {
                        description = slashCommandParameterService.getCommandOption(DESCRIPTION_PARAMETER, event, String.class);
                    } else {
                        description = null;
                    }

                    String durationString = slashCommandParameterService.getCommandOption(DURATION_PARAMETER, event, Duration.class, String.class);
                    Duration duration = ParseUtils.parseDuration(durationString);

                    GuildMessageChannel target = null;
                    if(slashCommandParameterService.hasCommandOption(CHANNEL_PARAMETER, event)) {
                        target = slashCommandParameterService.getCommandOption(CHANNEL_PARAMETER, event, GuildMessageChannel.class);
                    }

                    Integer winners = 1;
                    if(slashCommandParameterService.hasCommandOption(WINNERS_PARAMETER, event)) {
                        winners = slashCommandParameterService.getCommandOption(WINNERS_PARAMETER, event, Integer.class);
                    }

                    Member benefactor;
                    if(slashCommandParameterService.hasCommandOption(BENEFACTOR_PARAMETER, event)) {
                        benefactor = slashCommandParameterService.getCommandOption(BENEFACTOR_PARAMETER, event, Member.class);
                    } else {
                        benefactor = null;
                    }

                    Member creator = event.getMember();
                    GiveawayCreationRequest request = GiveawayCreationRequest
                            .builder()
                            .benefactor(benefactor)
                            .creator(creator)
                            .description(description)
                            .duration(duration)
                            .targetChannel(target)
                            .winnerCount(winners)
                            .title(title)
                            .build();

                    return giveawayService.createGiveaway(request)
                            .thenAccept(unused -> interactionService.sendEmbed(CREATE_GIVEAWAY_RESPONSE_TEMPLATE_KEY, interactionHook));
        }).thenApply(unused -> CommandResult.fromSuccess());

    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter titleParameter = Parameter
                .builder()
                .templated(true)
                .name(TITLE_PARAMETER)
                .type(String.class)
                .build();

        Parameter descriptionParameter = Parameter
                .builder()
                .templated(true)
                .name(DESCRIPTION_PARAMETER)
                .type(String.class)
                .optional(true)
                .build();

        Parameter channelParameter = Parameter
                .builder()
                .name(CHANNEL_PARAMETER)
                .type(GuildMessageChannel.class)
                .optional(true)
                .templated(true)
                .build();

        Parameter durationParameter = Parameter
                .builder()
                .name(DURATION_PARAMETER)
                .type(Duration.class)
                .templated(true)
                .build();

        Parameter winnersParameter = Parameter
                .builder()
                .name(WINNERS_PARAMETER)
                .type(Integer.class)
                .optional(true)
                .templated(true)
                .build();

        Parameter benefactorParameter = Parameter
                .builder()
                .templated(true)
                .name(BENEFACTOR_PARAMETER)
                .type(Member.class)
                .optional(true)
                .build();

        List<Parameter> parameters = Arrays.asList(titleParameter, durationParameter, benefactorParameter, descriptionParameter,
                channelParameter, winnersParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(GiveawaySlashCommandNames.GIVEAWAY)
                .commandName("create")
                .build();

        return CommandConfiguration.builder()
                .name(COMMAND_NAME)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return GiveawayFeatureDefinition.GIVEAWAY;
    }
}
