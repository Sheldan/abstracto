package dev.sheldan.abstracto.statistic.emotes.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.statistic.config.StatisticFeatures;
import dev.sheldan.abstracto.statistic.emotes.config.EmoteTrackingModule;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This command removes all {@link dev.sheldan.abstracto.statistic.emotes.model.database.UsedEmote} instances
 * and all {@link dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote} in a guild. This command cannot be undone.
 */
@Component
public class ResetEmoteStats extends AbstractConditionableCommand {

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        trackedEmoteService.resetEmoteStats(commandContext.getGuild());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("resetEmoteStats")
                .module(EmoteTrackingModule.EMOTE_TRACKING)
                .templated(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return StatisticFeatures.EMOTE_TRACKING;
    }
}
