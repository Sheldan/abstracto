package dev.sheldan.abstracto.experience.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a role which is given when the user reaches a certain level. These roles are configurable per server and
 * roles configured in this table are able to be set to a certain level.
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "experience_role")
@Getter
@Setter
public class AExperienceRole implements Serializable {

    /**
     * The abstracto unique id of this experience role.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the {@link AExperienceLevel} at which this role is awarded.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    private AExperienceLevel level;

    /**
     * Reference to the {@link AServer} at which this role is used as an experience role.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id", nullable = false)
    private AServer roleServer;

    /**
     * Reference to the actual {@link ARole} being awarded.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private ARole role;

    /**
     * Current list of {@link dev.sheldan.abstracto.core.models.database.AUserInAServer} which were given this role.
     */
    @OneToMany(fetch = FetchType.LAZY)
    @Builder.Default
    @JoinColumn(name = "experience_role_id")
    private List<AUserExperience> users = new ArrayList<>();
}
