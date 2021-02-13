package dev.sheldan.abstracto.experience.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.*;

import javax.persistence.*;
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

    @Id
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private ARole role;

    /**
     * Reference to the actual {@link ARole} being marked as disabled for experience gain.
     */
    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
