package dev.sheldan.abstracto.modmail.models.template;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * This is the model used when a new mod mail thread is opened and a message containing some information about the user
 * is displayed for the user handling the mod mail thread.
 */
@Getter
@Setter
@Builder
public class ModMailThreaderHeader {
    /**
     * A {@link FullUser} instance to retrieve information from
     */
    private FullUser threadUser;
    /**
     * The latest {@link ModMailThread}, before the current opened one. This is null if there is no closed mod mail thread
     * for the user
     */
    private ModMailThread latestModMailThread;
    /**
     * The amount of previous mod mail thread the user has.
     */
    private Long pastModMailThreadCount;
}
