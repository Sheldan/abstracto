package dev.sheldan.abstracto.statistic.emote.config;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import dev.sheldan.abstracto.statistic.config.StatisticModuleDefinition;
import org.springframework.stereotype.Component;

/**
 * Separate module just for all commands related to emote tracking.
 */
@Component
public class EmoteTrackingModuleDefinition implements ModuleDefinition {
    public static final String EMOTE_TRACKING = "emoteTracking";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(EMOTE_TRACKING).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return StatisticModuleDefinition.STATISTIC;
    }
}
