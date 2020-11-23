package dev.sheldan.abstracto.statistic.emotes.config;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleInterface;
import dev.sheldan.abstracto.statistic.config.StatisticModule;
import org.springframework.stereotype.Component;

/**
 * Separate module just for all commands related to emote tracking.
 */
@Component
public class EmoteTrackingModule implements ModuleInterface {
    public static final String EMOTE_TRACKING = "emoteTracking";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(EMOTE_TRACKING).description("Module containing commands related to emote tracking.").build();
    }

    @Override
    public String getParentModule() {
        return StatisticModule.STATISTIC;
    }
}
