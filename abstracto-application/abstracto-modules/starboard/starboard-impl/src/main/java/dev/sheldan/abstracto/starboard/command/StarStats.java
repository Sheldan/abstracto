package dev.sheldan.abstracto.starboard.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.starboard.config.StarboardFeatureDefinition;
import dev.sheldan.abstracto.starboard.config.StarboardSlashCommandNames;
import dev.sheldan.abstracto.starboard.model.template.MemberStarStatsModel;
import dev.sheldan.abstracto.starboard.service.StarboardService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class StarStats extends AbstractConditionableCommand {

    public static final String STARSTATS_RESPONSE_TEMPLATE = "starStats_response";
    public static final String STARSTATS_SINGLE_MEMBER_RESPONSE_TEMPLATE = "starStats_single_member_response";
    private static final String STAR_STATS_COMMAND = "starStats";
    private static final String MEMBER_PARAMETER = "member";

    @Autowired
    private StarboardService starboardService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        if(parameters.isEmpty()) {
            return starboardService.retrieveStarStats(commandContext.getGuild().getIdLong())
                    .thenCompose(starStatsModel -> {
                            MessageToSend messageToSend = templateService.renderEmbedTemplate(STARSTATS_RESPONSE_TEMPLATE, starStatsModel, commandContext.getGuild().getIdLong());
                            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()));
                    }).thenApply(o -> CommandResult.fromIgnored());
        } else {
            Member targetMember = (Member) parameters.get(0);
            MemberStarStatsModel memberStarStatsModel = starboardService.retrieveStarStatsForMember(targetMember);
            MessageToSend messageToSend = templateService.renderEmbedTemplate(STARSTATS_SINGLE_MEMBER_RESPONSE_TEMPLATE, memberStarStatsModel, commandContext.getGuild().getIdLong());
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromIgnored());
        }
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        if(slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
            Member targetMember = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class, Member.class);
            MemberStarStatsModel memberStarStatsModel = starboardService.retrieveStarStatsForMember(targetMember);
            MessageToSend messageToSend = templateService.renderEmbedTemplate(STARSTATS_SINGLE_MEMBER_RESPONSE_TEMPLATE, memberStarStatsModel, event.getGuild().getIdLong());
            return interactionService.replyMessageToSend(messageToSend, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        } else {
            return starboardService.retrieveStarStats(event.getGuild().getIdLong())
                    .thenCompose(starStatsModel -> {
                        MessageToSend messageToSend = templateService.renderEmbedTemplate(STARSTATS_RESPONSE_TEMPLATE, starStatsModel, event.getGuild().getIdLong());
                        return interactionService.replyMessageToSend(messageToSend, event);
                    }).thenApply(o -> CommandResult.fromIgnored());
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter memberParameter = Parameter
                .builder()
                .templated(true)
                .name(MEMBER_PARAMETER)
                .type(Member.class)
                .optional(true)
                .build();
        List<Parameter> parameters = Collections.singletonList(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(StarboardSlashCommandNames.STARBOARD)
                .commandName(STAR_STATS_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(STAR_STATS_COMMAND)
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
        return StarboardFeatureDefinition.STARBOARD;
    }
}
