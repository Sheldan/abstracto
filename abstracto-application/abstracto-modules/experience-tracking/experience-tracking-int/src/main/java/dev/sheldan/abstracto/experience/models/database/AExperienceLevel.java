package dev.sheldan.abstracto.experience.models.database;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "experience_level")
@Getter
@Setter
public class AExperienceLevel {
    @Id
    private Integer level;
    private Long experienceNeeded;
}
