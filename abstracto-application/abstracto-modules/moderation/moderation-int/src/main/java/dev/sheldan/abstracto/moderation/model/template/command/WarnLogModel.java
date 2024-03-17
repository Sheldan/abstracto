package dev.sheldan.abstracto.moderation.model.template.command;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

/**
 * Used when rendering the notification when a member was warned. The template is: "warn_log_embed"
 */
@Getter
@Builder
@Setter
public class WarnLogModel {
    /**
     * The reason why warn was cast
     */
    private String reason;
    /**
     * The {@link Member} being warned
     */
    private MemberDisplay warnedMember;
    private Long warnId;
    private Long infractionId;
    private MemberDisplay warningMember;
    private ServerChannelMessage channelMessage;
}
