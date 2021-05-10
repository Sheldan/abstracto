package dev.sheldan.abstracto.invitefilter.config;

import dev.sheldan.abstracto.core.config.PostTargetEnum;
import lombok.Getter;

@Getter
public enum InviteFilterPostTarget implements PostTargetEnum {
    INVITE_DELETE_LOG("inviteDeleteLog");

    private String key;

    InviteFilterPostTarget(String key) {
        this.key = key;
    }
}

