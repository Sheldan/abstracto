package dev.sheldan.abstracto.moderation.models.database;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * A warning which was given a member with a special reason by a moderating member. This warning is bound to a server.
 */
@Entity
@Table(name="warning")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Warning implements Serializable {

    /**
     * The globally unique id of this warning
     */
    @EmbeddedId
    @Getter
    @Setter
    private ServerSpecificId warnId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("serverId")
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    /**
     * The {@link AUserInAServer} which was warned
     */
    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warned_user_in_server_id", nullable = false)
    private AUserInAServer warnedUser;

    /**
     * The {@link AUserInAServer} which gave the warning
     */
    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warning_user_in_server_id", nullable = false)
    private AUserInAServer warningUser;

    /**
     * The reason why this warning was cast
     */
    @Getter
    @Setter
    @Column(name = "reason")
    private String reason;

    /**
     * The date at which the warning was cast
     */
    @Getter
    @Setter
    @Column(name = "warn_date")
    private Instant warnDate;

    /**
     * Whether or not the warning was already decayed and is not active anymore
     */
    @Getter
    @Setter
    @Column(name = "decayed")
    private Boolean decayed;

    /**
     * The date at which the warning was decayed
     */
    @Getter
    @Setter
    @Column(name = "decay_date")
    private Instant decayDate;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
