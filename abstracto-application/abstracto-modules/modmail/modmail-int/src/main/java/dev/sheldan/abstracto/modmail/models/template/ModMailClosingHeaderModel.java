package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

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
    private ModMailThread closedThread;

    /**
     * The duration between the creation and closed date of a {@link ModMailThread}
     * @return
     */
    public Duration getDuration() {
        return Duration.between(closedThread.getCreated(), closedThread.getClosed());
    }
}
