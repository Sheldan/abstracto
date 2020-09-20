package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.SlimUserInitiatedServerContext;
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
public class KickLogModel extends SlimUserInitiatedServerContext {
    /**
     * The reason of the kick
     */
    private String reason;
    /**
     * The member being kicked
     */
    private Member kickedUser;
}
