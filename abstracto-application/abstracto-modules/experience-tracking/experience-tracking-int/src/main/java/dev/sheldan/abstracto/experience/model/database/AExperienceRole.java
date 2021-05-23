package dev.sheldan.abstracto.experience.model.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a role which is given when the user reaches a certain level. These roles are configurable per server and
 * roles configured in this table are able to be set to a certain level.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "experience_role")
@Getter
@Setter
@EqualsAndHashCode
public class AExperienceRole implements Serializable {

    /**
     * The ID of the {@link ARole} to be awarded at a certain experience level
     */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Reference to the {@link ARole} which is being awarded for this configuration
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private ARole role;

    /**
     * Reference to the {@link AExperienceLevel} at which this role is awarded.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private AExperienceLevel level;

    /**
     * Reference to the {@link AServer} at which this role is used as an experience role.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    /**
     * The {@link Instant} this entity was created
     */
    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    /**
     * The {@link Instant} this entity was updated
     */
    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    /**
     * Current list of {@link dev.sheldan.abstracto.core.models.database.AUserInAServer} which were given this role.
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "currentExperienceRole")
    @Builder.Default
    private List<AUserExperience> users = new ArrayList<>();


}
