package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.moderation.model.database.Warning;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

/**
 * Used when rendering the notification when a member was warned. The template is: "warn_log_embed"
 */
@Getter
@Builder
@Setter
public class WarnContext {
    /**
     * The reason why the warn was cast
     */
    private String reason;
    /**
     * The {@link Member} being warned
     */
    private Member warnedMember;
    /**
     * The persisted {@link Warning} object from the database containing the information about the warning
     */
    private Long warnId;
    private Long infractionId;
    private Member member;
    private Guild guild;
    private Message message;
    private GuildMessageChannel channel;
}
