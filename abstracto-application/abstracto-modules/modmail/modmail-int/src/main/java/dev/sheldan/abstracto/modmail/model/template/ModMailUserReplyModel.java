package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;
import java.util.Map;

/**
 * Model used to render the response by the user to the mod mail thread channel.
 */
@Getter
@Setter
@Builder
public class ModMailUserReplyModel {
    private UserDisplay userDisplay;
    /**
     * The {@link Message} which was posted, which contains all the possible information
     */
    private Message postedMessage;
    private List<String> attachedImageUrls;
    private Map<String, String> remainingAttachments;
    /**
     * List of {@link Member} which are registered as subscribers for a particular mod mail thread and will be pinged
     * when the user sends a new message
     */
    private List<Member> subscribers;
}
