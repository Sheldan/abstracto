package dev.sheldan.abstracto.experience.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

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
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AExperienceLevel implements Serializable {
    /**
     * The unique level from 0 to as defined in the configuration. Will be created on startup.
     */
    @Id
    private Integer level;
    /**
     * The total amount of experience needed for this level.
     */
    private Long experienceNeeded;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AExperienceLevel that = (AExperienceLevel) o;
        return Objects.equals(level, that.level) &&
                Objects.equals(experienceNeeded, that.experienceNeeded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, experienceNeeded);
    }
}
