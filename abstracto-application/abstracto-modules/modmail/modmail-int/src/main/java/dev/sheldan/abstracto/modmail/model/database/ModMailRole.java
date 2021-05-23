package dev.sheldan.abstracto.modmail.model.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

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
@Table(name = "mod_mail_role")
@Getter
@Setter
@EqualsAndHashCode
public class ModMailRole implements Serializable {

    /**
     * Unique ID of the mod mail role
     */
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long modMailRoleId;

    /**
     * Which {@link AServer} this role is associated with, for convenience
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    /**
     * The actual {@link ARole} which this mod mail role references
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private ARole role;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
