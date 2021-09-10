package dev.sheldan.abstracto.statistic.emote.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticFeatureDefinition;
import dev.sheldan.abstracto.statistic.emote.config.EmoteTrackingModuleDefinition;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.UsedEmoteService;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * This command will delete the instances of {@link dev.sheldan.abstracto.statistic.emote.model.database.UsedEmote} of a given {@link TrackedEmote}
 * for the desired {@link Duration}, or all of them. This command cannot be undone.
 */
@Component
public class PurgeEmoteStats extends AbstractConditionableCommand {

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Autowired
    private UsedEmoteService usedEmoteService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        TrackedEmote fakeTrackedEmote = (TrackedEmote) parameters.get(0);
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByTrackedEmoteServer(fakeTrackedEmote.getTrackedEmoteId());
        // default 1.1.1970
        Instant since = Instant.EPOCH;
        if(parameters.size() > 1) {
            // if a Duration is given, subtract it from the current point in time
            Duration parameter = (Duration) parameters.get(1);
            since = Instant.now().minus(parameter);
        }
        usedEmoteService.purgeEmoteUsagesSince(trackedEmote, since);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("trackedEmote").templated(true).type(TrackedEmote.class).build());
        parameters.add(Parameter.builder().name("period").templated(true).optional(true).type(Duration.class).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("purgeEmoteStats")
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .supportsEmbedException(true)
                .requiresConfirmation(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return StatisticFeatureDefinition.EMOTE_TRACKING;
    }
}
