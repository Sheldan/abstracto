package dev.sheldan.abstracto.modmail.model.template;

import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.time.Instant;

/**
 * This is the model used when a new mod mail thread is opened and a message containing some information about the user
 * is displayed for the user handling the mod mail thread.
 */
@Getter
@Setter
@Builder
public class ModMailThreaderHeader {
    /**
     * A {@link Member} instance to retrieve information from
     */
    private Member member;
    /**
     * The latest {@link ModMailThread}, before the current opened one. This is null if there is no closed mod mail thread
     * for the user
     */
    private ModMailThread latestModMailThread;
    private Instant memberJoinDate;
    /**
     * The amount of previous mod mail thread the user has.
     */
    private Long pastModMailThreadCount;
}
