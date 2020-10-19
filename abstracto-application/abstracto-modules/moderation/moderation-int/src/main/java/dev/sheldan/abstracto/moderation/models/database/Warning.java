package dev.sheldan.abstracto.moderation.models.database;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
    @ManyToOne
    @JoinColumn(name = "warnedUserId", nullable = false)
    private AUserInAServer warnedUser;

    /**
     * The {@link AUserInAServer} which gave the warning
     */
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "warningUserId", nullable = false)
    private AUserInAServer warningUser;

    /**
     * The reason why this warning was cast
     */
    @Getter
    @Setter
    private String reason;

    /**
     * The date at which the warning was cast
     */
    @Getter
    @Setter
    private Instant warnDate;

    /**
     * Whether or not the warning was already decayed and is not active anymore
     */
    @Getter
    @Setter
    private Boolean decayed;

    /**
     * The date at which the warning was decayed
     */
    @Getter
    @Setter
    private Instant decayDate;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    @Column(name = "updated")
    private Instant updated;

    @PreUpdate
    private void onUpdate() {
        this.updated = Instant.now();
    }

}
