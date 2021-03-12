package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;


/**
 * Used when rendering the notification when a member was banned. The template is: "ban_log_embed"
 */
@Getter
@SuperBuilder
@Setter
public class BanLog extends UserInitiatedServerContext {
    /**
     * The reason of the ban
     */
    private String reason;
    /**
     * The member executing the ban
     */
    private Member banningUser;
    /**
     * The member being banned
     */
    private Member bannedUser;
}
