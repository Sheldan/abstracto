package dev.sheldan.abstracto.moderation.model.template.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

/**
 * Used when rendering the notification when a member was kicked. The template is: "kick_log_embed"
 */
@Getter
@Builder
@Setter
public class KickLogModel {
    /**
     * The reason of the kick
     */
    private String reason;
    /**
     * The member being kicked
     */
    private Member kickedUser;
    private Member member;
    private Guild guild;
    private GuildMessageChannel channel;
}
