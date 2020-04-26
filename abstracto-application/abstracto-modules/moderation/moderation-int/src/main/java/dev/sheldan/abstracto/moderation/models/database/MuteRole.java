package dev.sheldan.abstracto.moderation.models.database;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import javax.persistence.*;

/**
 * Represents a role to be used for muting users on a certain server
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mute_role")
@Getter
@Setter
public class MuteRole {

    /**
     * The abstracto unique id of this mute role.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the {@link AServer} at which this role is used as an mute role.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id", nullable = false)
    private AServer roleServer;

    /**
     * Reference to the actual {@link ARole} being used to mute.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private ARole role;
}