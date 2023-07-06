package dev.sheldan.abstracto.assignableroles.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assignable_role_place")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AssignableRolePlace implements Serializable {

    public static final Long ASSIGNABLE_PLACE_NAME_LIMIT = 255L;
    public static final Long ASSIGNABLE_PLACE_DESCRIPTION_LIMIT = 255L;

    /**
     * A unique ID created
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="channel_id", nullable = false)
    private AChannel channel;

    /**
     * The {@link AServer server} for which this place is configured for. Unique in combination with the key
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    /**
     * The key this place is associated with via commands. Unique per server.
     */
    @Column(name = "key", nullable = false)
    private String key;

    /**
     * A List containing the {@link AssignableRole} which are associated with this place
     */
    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "assignablePlace"
    )
    @Builder.Default
    private List<AssignableRole> assignableRoles = new ArrayList<>();

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "text", nullable = false)
    private String text;

    /**
     * Whether or not it should be restricted, that a {@link AssignedRoleUser} should only have one role of this place
     */
    @Builder.Default
    @Column(name = "unique_roles", nullable = false)
    private Boolean uniqueRoles = false;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AssignableRolePlaceType type;

}
