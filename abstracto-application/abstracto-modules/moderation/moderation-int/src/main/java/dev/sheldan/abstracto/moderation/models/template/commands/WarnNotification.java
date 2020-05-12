package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.moderation.models.database.Warning;
import lombok.Builder;
import lombok.Value;

/**
 * Used to render the message notification send to the member informing about the warn. The template is: "warn_notification"
 */
@Value
@Builder
public class WarnNotification {
    /**
     * The persisted mute object from the database containing the information about the warning
     */
    private Warning warning;
    /**
     * The name of the server on which the warn was cast
     */
    private String serverName;
}
