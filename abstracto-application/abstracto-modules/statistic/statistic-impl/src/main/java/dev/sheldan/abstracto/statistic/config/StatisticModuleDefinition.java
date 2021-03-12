package dev.sheldan.abstracto.statistic.config;

import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import org.springframework.stereotype.Component;

/**
 * Help module for all statistics related commands, they have their own module if they have a significant emount of commands on their own.
 */
@Component
public class StatisticModuleDefinition implements ModuleDefinition {

    public static final String STATISTIC = "statistic";

    @Override
    public ModuleInfo getInfo() {
        return ModuleInfo.builder().name(STATISTIC).templated(true).build();
    }

    @Override
    public String getParentModule() {
        return "default";
    }
}
