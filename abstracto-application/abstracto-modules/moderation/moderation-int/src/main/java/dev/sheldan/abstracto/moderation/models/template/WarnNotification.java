package dev.sheldan.abstracto.moderation.models.template;

import dev.sheldan.abstracto.moderation.models.Warning;
import lombok.Builder;
import lombok.Value;

@Value @Builder
public class WarnNotification {
    private Warning warning;
    private String serverName;
}
