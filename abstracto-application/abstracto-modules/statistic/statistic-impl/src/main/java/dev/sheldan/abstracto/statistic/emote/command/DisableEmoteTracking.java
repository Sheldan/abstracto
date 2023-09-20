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
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import dev.sheldan.abstracto.statistic.emote.service.management.TrackedEmoteManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This command disables the tracking for either one {@link TrackedEmote} or for all of them, if no emote is given as a parameter
 */
@Component
public class DisableEmoteTracking extends AbstractConditionableCommand {

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private TrackedEmoteManagementService trackedEmoteManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        if(!parameters.isEmpty()) {
            TrackedEmote fakeTrackedEmote = (TrackedEmote) parameters.get(0);
            // need to reload the tracked emote
            TrackedEmote trackedEmote = trackedEmoteManagementService.loadByTrackedEmoteServer(fakeTrackedEmote.getTrackedEmoteId());
            trackedEmoteManagementService.disableTrackedEmote(trackedEmote);
        } else {
            trackedEmoteService.disableEmoteTracking(commandContext.getGuild());
        }
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter trackedEmoteParameter = Parameter
                .builder()
                .name("trackedEmote")
                .templated(true)
                .optional(true)
                .type(TrackedEmote.class)
                .build();
        parameters.add(trackedEmoteParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("disableEmoteTracking")
                .module(EmoteTrackingModuleDefinition.EMOTE_TRACKING)
                .templated(true)
                .messageCommandOnly(true)
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
}
