package dev.sheldan.abstracto.logging.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum  LoggingPostTarget implements PostTargetEnum {
    LEAVE_LOG("leaveLog"),
    JOIN_LOG("joinLog"),
    DELETE_LOG("deleteLog"),
    EDIT_LOG("editLog");

    private String key;

    LoggingPostTarget(String key) {
        this.key = key;
    }
}

