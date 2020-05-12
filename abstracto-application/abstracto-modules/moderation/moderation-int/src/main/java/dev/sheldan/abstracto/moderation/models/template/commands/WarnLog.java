package dev.sheldan.abstracto.moderation.models.template.commands;

import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

/**
 * Used when rendering the notification when a member was warned. The template is: "warn_log_embed"
 */
@Getter
@SuperBuilder
@Setter
public class WarnLog extends UserInitiatedServerContext {
    /**
     * The reason why the warn was cast
     */
    private String reason;
    /**
     * The {@link Member} being warned
     */
    private Member warnedUser;
    /**
     * The {@link Member} casting the warn
     */
    private Member warningUser;
    /**
     * The {@link Message} which contained the command triggering the warn
     */
    private Message message;
    /**
     * The persisted {@link Warning} object from the database containing the information about the warning
     */
    private Warning warning;
}
