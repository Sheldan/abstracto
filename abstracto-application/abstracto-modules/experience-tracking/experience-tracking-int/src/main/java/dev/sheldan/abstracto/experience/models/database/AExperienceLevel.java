package dev.sheldan.abstracto.experience.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * Represents an existing level to reach and the total necessary experience needed to reach that level.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "experience_level")
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AExperienceLevel implements Serializable {
    /**
     * The unique level from 0 to as defined in the configuration. Will be created on startup.
     */
    @Id
    @Column(name = "level")
    private Integer level;
    /**
     * The total amount of experience needed for this level.
     */
    @Column(name = "experience_needed")
    private Long experienceNeeded;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
