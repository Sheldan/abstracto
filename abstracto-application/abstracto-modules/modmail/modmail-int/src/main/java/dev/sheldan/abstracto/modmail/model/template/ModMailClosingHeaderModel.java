package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;
import java.time.Instant;

/**
 * This model is used when rendering the message before logging the messages in a closed {@link ModMailThread} and contains
 * general information about why the thread was closed and the thread itself.
 */
@Getter
@Setter
@Builder
public class ModMailClosingHeaderModel {
    /**
     * The note used to close the thread, might be the default value
     */
    private String note;
    /**
     * The {@link ModMailThread} which was closed
     */
    private Integer messageCount;
    private Instant startDate;
    private Long userId;

    /**
     * The duration between the creation and closed date of a {@link ModMailThread}
     * @return The duration between the creation date and the date the thread has been closed
     */
    public Duration getDuration() {
        return Duration.between(startDate, Instant.now());
    }

    private Member closingMember;
    private Boolean silently;
    private UserDisplay user;
    private Long serverId;
    private Long modmailThreadId;
}
