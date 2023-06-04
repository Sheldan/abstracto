package dev.sheldan.abstracto.suggestion.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum PollPostTarget implements PostTargetEnum {
    POLLS("polls"), POLL_REMINDER("pollReminder");

    private String key;

    PollPostTarget(String key) {
        this.key = key;
    }
}
