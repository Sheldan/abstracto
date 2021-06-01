package dev.sheldan.abstracto.moderation.model.template.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;


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
    private Member unBanningMember;
    /**
     * The user being unbanned
     */
    private User bannedUser;
    private Message commandMessage;
}
