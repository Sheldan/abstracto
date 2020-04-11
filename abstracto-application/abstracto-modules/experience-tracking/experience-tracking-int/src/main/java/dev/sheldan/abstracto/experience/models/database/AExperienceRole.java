package dev.sheldan.abstracto.experience.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "experience_role")
@Getter
@Setter
public class AExperienceRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "level_id")
    private AExperienceLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id")
    private AServer roleServer;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "role_id")
    private ARole role;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    @JoinColumn(name = "experience_role_id")
    private List<AUserExperience> users = new ArrayList<>();
}
