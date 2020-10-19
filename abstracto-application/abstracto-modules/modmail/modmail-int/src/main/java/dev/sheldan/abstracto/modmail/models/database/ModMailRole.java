package dev.sheldan.abstracto.modmail.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * The table responsible to define which roles are the roles responsible for handling the mod mail threads.
 * These will get notified via a ping, when a new mod mail thread is created.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "modmail_role")
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ModMailRole implements Serializable {

    /**
     * Unique ID of the mod mail role
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modMailRoleId;

    /**
     * Which {@link AServer} this role is associated with, for convenience
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modmail_role_server", nullable = false)
    private AServer server;

    /**
     * The actual {@link ARole} which this mod mail role references
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modmail_role", nullable = false)
    private ARole role;

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
