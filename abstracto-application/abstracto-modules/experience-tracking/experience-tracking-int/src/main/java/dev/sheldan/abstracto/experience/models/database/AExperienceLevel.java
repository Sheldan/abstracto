package dev.sheldan.abstracto.experience.models.database;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

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
}
