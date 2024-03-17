package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Used when rendering the notification when a member was banned. The template is: "ban_log_embed"
 */
@Getter
@Builder
@Setter
public class UnBanLog {
    /**
     * The member executing the unban
     */
    private MemberDisplay unBanningMember;
    /**
     * The user being unbanned
     */
    private UserDisplay bannedUser;
}
