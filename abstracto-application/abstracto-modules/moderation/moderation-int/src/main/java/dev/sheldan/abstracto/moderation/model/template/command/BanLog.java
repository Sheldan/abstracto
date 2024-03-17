package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;


/**
 * Used when rendering the notification when a member was banned. The template is: "ban_log_embed"
 */
@Getter
@Builder
@Setter
public class BanLog {
    /**
     * The reason of the ban
     */
    private String reason;
    /**
     * The member executing the ban
     */
    private MemberDisplay banningMember;
    /**
     * The user being banned
     */
    private UserDisplay bannedUser;
    private Duration deletionDuration;
}
