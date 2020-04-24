package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.moderation.models.database.Mute;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MuteNotification {
    private Mute mute;
    private String serverName;
}
