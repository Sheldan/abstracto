package dev.sheldan.abstracto.modmail.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum  ModMailPostTargets implements PostTargetEnum {
    MOD_MAIL_PING("modmailPing"), MOD_MAIL_LOG("modmailLog");

    private String key;

    ModMailPostTargets(String key) {
        this.key = key;
    }
}
