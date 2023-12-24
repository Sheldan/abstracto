package dev.sheldan.abstracto.modmail.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum  ModMailPostTargets implements PostTargetEnum {
    /**
     * The channel to be used for notifying the users about new mod mail threads
     */
    MOD_MAIL_PING("modmailPing"),
    /**
     * The channel to be used to log the contents of closed  mod mail threads
     */
    MOD_MAIL_LOG("modmailLog"),
    /**
     * The thread used as a container for the modmail threads
     */
    MOD_MAIL_CONTAINER("modmailContainer");

    private String key;

    ModMailPostTargets(String key) {
        this.key = key;
    }
}
