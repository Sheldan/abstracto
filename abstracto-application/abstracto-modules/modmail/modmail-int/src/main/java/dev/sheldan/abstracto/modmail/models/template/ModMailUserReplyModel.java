package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.FullUserInServer;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * Model used to render the response by the user to the mod mail thread channel.
 */
@Getter
@Setter
@Builder
public class ModMailUserReplyModel {
    /**
     * The {@link FullUserInServer} from which the message is and whose mod mail thread it is
     */
    private FullUserInServer threadUser;
    /**
     * The {@link Message} which was posted, which contains all the possible information
     */
    private Message postedMessage;
    /**
     * The {@link ModMailThread} for which the {@link Message} was a reply for
     */
    private ModMailThread modMailThread;
    /**
     * List of {@link FullUserInServer} which are registered as subscribers for a particular mod mail thread and will be pinged
     * when the user sends a new message
     */
    private List<FullUserInServer> subscribers;
}
