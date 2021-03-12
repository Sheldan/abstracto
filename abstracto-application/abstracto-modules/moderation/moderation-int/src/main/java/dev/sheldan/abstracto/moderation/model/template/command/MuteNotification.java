package dev.sheldan.abstracto.moderation.model.template.command;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Used to render the message notification send to the member informing about the mute. The template is: "mute_notification"
 */
@Value
@Builder
public class MuteNotification {
    /**
     * The mute context providing the necessary information about the mute
     */
    private String reason;
    private Instant muteTargetDate;
    /**
     * The name of the server in which the user was muted.
     */
    private String serverName;
}
