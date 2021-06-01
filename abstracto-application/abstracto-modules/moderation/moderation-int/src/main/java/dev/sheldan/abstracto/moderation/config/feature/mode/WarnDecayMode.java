package dev.sheldan.abstracto.moderation.config.feature.mode;


import dev.sheldan.abstracto.core.config.FeatureMode;
import lombok.Getter;

@Getter
public enum WarnDecayMode implements FeatureMode {
    AUTOMATIC_WARN_DECAY_LOG("automaticWarnDecayLogging"),
    NOTIFY_MEMBER_WARNING_DECAYS("notifyMemberWarningDecays")
    ;

    private final String key;

    WarnDecayMode(String key) {
        this.key = key;
    }

}