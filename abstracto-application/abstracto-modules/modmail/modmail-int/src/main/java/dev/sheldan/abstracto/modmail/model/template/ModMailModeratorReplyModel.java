package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;
import java.util.Map;

/**
 * Model used to render the response by staff members to the DM channel with the user
 */
@Getter
@Setter
@Builder
public class ModMailModeratorReplyModel {
    private UserDisplay userDisplay;
    /**
     * The staff {@link Member} which replied to the thread, be it anonymously or normal.
     */
    private Member moderator;
    private String text;
    /**
     * Whether or not the reply should be shown anonymous
     */
    private Boolean anonymous;
    private List<String> attachedImageUrls;
    private Map<String, String> remainingAttachments;
    /**
     * The {@link ModMailThread} to reply to
     */
    private ModMailThread modMailThread;
}
