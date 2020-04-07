package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.moderation.models.database.Warning;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class WarnNotification {
    private Warning warning;
    private String serverName;
}
