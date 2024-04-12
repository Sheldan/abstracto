package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CombinedParameterEntry {
    private Class type;
    private boolean usableInSlashCommands;
    private boolean usableInMessageCommands;

    public static CombinedParameterEntry slashParameter(Class type) {
        return CombinedParameterEntry
                .builder()
                .type(type)
                .usableInSlashCommands(true)
                .build();
    }

    public static CombinedParameterEntry messageParameter(Class type) {
        return CombinedParameterEntry
                .builder()
                .type(type)
                .usableInMessageCommands(true)
                .build();
    }

    public static CombinedParameterEntry parameter(Class type) {
        return CombinedParameterEntry
                .builder()
                .type(type)
                .usableInMessageCommands(true)
                .usableInSlashCommands(true)
                .build();
    }
}
