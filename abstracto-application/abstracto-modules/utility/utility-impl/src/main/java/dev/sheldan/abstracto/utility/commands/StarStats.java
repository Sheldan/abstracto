package dev.sheldan.abstracto.utility.commands;

import dev.sheldan.abstracto.core.command.UtilityModuleInterface;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.template.commands.starboard.MemberStarStatsModel;
import dev.sheldan.abstracto.utility.service.StarboardService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class StarStats extends AbstractConditionableCommand {

    public static final String STARSTATS_RESPONSE_TEMPLATE = "starStats_response";
    public static final String STARSTATS_SINGLE_MEMBER_RESPONSE_TEMPLATE = "starStats_single_member_response";

    @Autowired
    private StarboardService starboardService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        if(parameters.isEmpty()) {
            return starboardService.retrieveStarStats(commandContext.getGuild().getIdLong())
                    .thenCompose(starStatsModel ->
                            FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInChannel(STARSTATS_RESPONSE_TEMPLATE, starStatsModel, commandContext.getChannel()))
                    ).thenApply(o -> CommandResult.fromIgnored());
        } else {
            Member targetMember = (Member) parameters.get(0);
            MemberStarStatsModel memberStarStatsModel = starboardService.retrieveStarStatsForMember(targetMember);
            return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInChannel(STARSTATS_SINGLE_MEMBER_RESPONSE_TEMPLATE, memberStarStatsModel, commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromIgnored());
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter memberParameter = Parameter.builder().templated(true).name("member").type(Member.class).optional(true).build();
        List<Parameter> parameters = Collections.singletonList(memberParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("starStats")
                .module(UtilityModuleInterface.UTILITY)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.STARBOARD;
    }
}
