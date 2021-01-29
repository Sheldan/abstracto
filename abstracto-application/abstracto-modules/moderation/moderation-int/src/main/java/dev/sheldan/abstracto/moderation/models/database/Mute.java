package dev.sheldan.abstracto.moderation.models.database;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * Table used to store mutes in order to track when the mute was cast and when it ended.
 */
@Entity
@Table(name="mute")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Mute implements Serializable {

    /**
     * The globally unique id of the mute.
     */
    @EmbeddedId
    private ServerSpecificId muteId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("serverId")
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    /**
     * The {@link AUserInAServer} which was muted
     */
    @ManyToOne
    @JoinColumn(name = "mutedUser", nullable = false)
    private AUserInAServer mutedUser;

    /**
     * The {@link AUserInAServer} which casted the mute
     */
    @ManyToOne
    @JoinColumn(name = "mutingUser", nullable = false)
    private AUserInAServer mutingUser;

    /**
     * The reason of the mute which is stored
     */
    private String reason;

    /**
     * The date when the mute was cast, and the start date
     */
    private Instant muteDate;

    /**
     * The date at which this mute should be removed in the future
     */
    private Instant muteTargetDate;

    /**
     * Whether or not the mute already ended, be it manually or when the time passed
     */
    private Boolean muteEnded;

    /**
     * The message which contained the command which caused this mute
     */
    @Column
    private Long messageId;

    /**
     * The channel in which this mute was cast
     */
    @ManyToOne
    @JoinColumn(name = "mutingChannel", nullable = false)
    private AChannel mutingChannel;

    /**
     * When the mute is scheduled to be un-done with quartz, this stores the quartz trigger in order to cancel it, if need be.
     */
    private String triggerKey;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
        this.muteDate = Instant.now();
    }

    @Column(name = "updated")
    private Instant updated;

    @PreUpdate
    private void onUpdate() {
        this.updated = Instant.now();
    }

}
