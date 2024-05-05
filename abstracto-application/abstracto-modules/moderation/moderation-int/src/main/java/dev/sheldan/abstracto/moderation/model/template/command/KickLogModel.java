package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Used when rendering the notification when a member was kicked. The template is: "kick_log_embed"
 */
@Getter
@Builder
@Setter
public class KickLogModel {
    private String reason;
    private UserDisplay kickedUser;
    private UserDisplay kickingUser;
}
