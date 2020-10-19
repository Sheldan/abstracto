package dev.sheldan.abstracto.experience.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

/**
 * A role for which the experience gain in a particular server has been disabled.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "disabled_experience_roles")
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ADisabledExpRole implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the actual {@link ARole} being marked as disabled for experience gain.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private ARole role;
}
