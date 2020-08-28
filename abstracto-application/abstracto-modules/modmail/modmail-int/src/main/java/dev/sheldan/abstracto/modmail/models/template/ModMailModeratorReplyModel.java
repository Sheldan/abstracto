package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

/**
 * Model used to render the response by staff members to the DM channel with the user
 */
@Getter
@Setter
@Builder
public class ModMailModeratorReplyModel {
    /**
     * A {@link FullUserInServer} reference representing the user the thread is about. The member attribute is null, if the user left the guild
     */
    private FullUserInServer threadUser;
    /**
     * The staff {@link Member} which replied to the thread, be it anonymously or normal.
     */
    private Member moderator;
    /**
     * The text which was used to reply. This is necessary, because the reply is triggered via a command, so
     * we would need re-parse the {@link Message} in order to find the value to display
     */
    private String text;
    /**
     * The {@link Message} which contained the command to reply to the user. This is needed for attachments.
     */
    private Message postedMessage;
    /**
     * Whether or not the reply should be shown anonymous
     */
    private Boolean anonymous;
    /**
     * The {@link ModMailThread} to reply to
     */
    private ModMailThread modMailThread;
}
