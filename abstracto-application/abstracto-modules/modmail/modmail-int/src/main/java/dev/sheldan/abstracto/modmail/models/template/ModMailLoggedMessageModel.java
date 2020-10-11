package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

/**
 * This model is used to render a message from a mod mail thread when closing the thread and logging the thread to the logging post target
 */
@Getter
@Setter
@Builder
public class ModMailLoggedMessageModel {
    /**
     * The {@link Message} instance which was posted
     */
    private Message message;
    /**
     * The reference to the {@link ModMailMessage} stored in the database
     */
    private ModMailMessage modMailMessage;

    /**
     * A reference to the {@link FullUserInServer} which is the author. The member part is null, if the member left the guild.
     */
    private Member author;


}
