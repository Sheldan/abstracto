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
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import dev.sheldan.abstracto.statistic.emotes.service.management.TrackedEmoteManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This command completely deletes one individual {@link TrackedEmote} and all of its usages from the database. This command cannot be undone.
 */
@Component
public class DeleteTrackedEmote extends AbstractConditionableCommand {

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        TrackedEmote fakeTrackedEmote = (TrackedEmote) parameters.get(0);
        // need to actually load the TrackedEmote
        TrackedEmote trackedEmote = trackedEmoteManagementService.loadByTrackedEmoteServer(fakeTrackedEmote.getTrackedEmoteId());
        trackedEmoteService.deleteTrackedEmote(trackedEmote);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("trackedEmote").templated(true).type(TrackedEmote.class).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("deleteTrackedEmote")
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
