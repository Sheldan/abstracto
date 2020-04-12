package dev.sheldan.abstracto.experience.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_experience")
@Getter
@Setter
public class AUserExperience {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private AUserInAServer user;

    private Long experience;

    private Long messageCount;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "level_id")
    private AExperienceLevel currentLevel;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "experience_role_id")
    private AExperienceRole currentExperienceRole;
}
