package dev.sheldan.abstracto.modmail.config;

import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

/**
 * This enum defines the to {@link FeatureMode} mod mail has.
 * Either closed mod mail threads are logged, or they are not logged to the respective post target.
 */
@Getter
public enum  ModMailMode implements FeatureMode {
    LOGGING("log"),
    SEPARATE_MESSAGE("threadMessage"),
    THREAD_CONTAINER("threadContainer"),
    MOD_MAIL_APPEALS("modMailAppeals");

    private final String key;

    ModMailMode(String key) {
        this.key = key;
    }

}
