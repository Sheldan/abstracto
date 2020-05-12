package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

/**
 * Used when rendering the notification when a member was kicked. The template is: "kick_log_embed"
 */
@Getter
@SuperBuilder
@Setter
public class KickLogModel extends UserInitiatedServerContext {
    /**
     * The reason of the kick
     */
    private String reason;
    /**
     * The member executing the kick
     */
    private Member kickingUser;
    /**
     * The member being kicked
     */
    private Member kickedUser;
}
