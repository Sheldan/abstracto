package dev.sheldan.abstracto.moderation.models;

import lombok.Builder;
import lombok.Value;

@Value @Builder
public class WarnNotification {
    private Warning warning;
    private String serverName;
}
