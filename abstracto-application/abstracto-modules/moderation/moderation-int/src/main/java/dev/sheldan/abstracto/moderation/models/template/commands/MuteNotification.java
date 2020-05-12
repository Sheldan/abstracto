package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.moderation.models.database.Mute;
import lombok.Builder;
import lombok.Value;

/**
 * Used to render the message notification send to the member informing about the mute. The template is: "mute_notification"
 */
@Value
@Builder
public class MuteNotification {
    /**
     * The persisted mute object from the database containing the information about the mute
     */
    private Mute mute;
    /**
     * The name of the server in which the user was muted.
     */
    private String serverName;
}
