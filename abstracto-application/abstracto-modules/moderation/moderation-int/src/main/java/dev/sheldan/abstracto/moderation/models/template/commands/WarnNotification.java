package dev.sheldan.abstracto.moderation.models.template.commands;

import lombok.Builder;
import lombok.Value;

/**
 * Used to render the message notification send to the member informing about the warn. The template is: "warn_notification"
 */
@Value
@Builder
public class WarnNotification {
    /**
     * The reason of the warning
     */
    private String reason;
    private Long warnId;
    /**
     * The name of the server on which the warn was cast
     */
    private String serverName;
}
