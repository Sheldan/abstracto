package dev.sheldan.abstracto.experience.model.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * A role for which the experience gain in a particular server has been disabled.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "disabled_experience_role")
@Getter
@Setter
@EqualsAndHashCode
public class ADisabledExpRole implements Serializable {

    /**
     * The ID of the {@link ARole role} which is being marked to be used as a marker for {@link net.dv8tion.jda.api.entities.Member} which should not gain experience
     */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Reference to the actual {@link ARole} being marked as disabled for experience gain.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private ARole role;

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
}
