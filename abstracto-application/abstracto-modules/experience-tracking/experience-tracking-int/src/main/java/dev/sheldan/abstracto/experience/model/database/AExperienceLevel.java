package dev.sheldan.abstracto.experience.model.database;

import lombok.*;

import jakarta.persistence.*;
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
public class AExperienceLevel implements Serializable {

    /**
     * The unique level from 0 to as defined in the configuration. Will be created on startup.
     */
    @Id
    @Column(name = "level", nullable = false)
    private Integer level;

    /**
     * The total amount of experience needed for this level.
     */
    @Column(name = "experience_needed", nullable = false)
    private Long experienceNeeded;

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
