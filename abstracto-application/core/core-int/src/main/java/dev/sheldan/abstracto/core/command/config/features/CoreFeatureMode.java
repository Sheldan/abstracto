package dev.sheldan.abstracto.core.command.config.features;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum CoreFeatureMode implements FeatureMode {
    SUGGEST_SLASH_COMMANDS("suggestSlashCommands");

    private final String key;

    CoreFeatureMode(String key) {
        this.key = key;
    }
}
