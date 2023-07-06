package dev.sheldan.abstracto.assignableroles.model.database;

import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ARole role} which can be mapped to an {@link AssignableRolePlace ṕlace}. This is uniquely defined by an emote on the
 * respective assignable role place, but the same role can be given via different {@link AEmote emote}
 */
@Entity
@Table(name = "assignable_role")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AssignableRole implements Serializable {

    /**
     * The unique ID of this {@link AssignableRole assignableRole}
     */
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emote_markdown")
    private String emoteMarkdown;

    /**
     * The {@link ARole} which given via this {@link AssignableRole assignableRole}
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private ARole role;

    /**
     * The {@link AServer server} in which this {@link AssignableRole assignableRole} is used
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    /**
     * The {@link AssignableRolePlace} this assignable role is in
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "assignable_place_id", nullable = false)
    private AssignableRolePlace assignablePlace;

    /**
     * The {@link AssignedRoleUser users} which currently have this role assigned via this mechanism.
     * This is necessary to enforce the unique property of {@link AssignableRolePlace}, in which you only may chose one
     * role.
     */
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private List<AssignedRoleUser> assignedUsers = new ArrayList<>();

    /**
     * The display text which is used for the button
     */
    @Column(name = "description", nullable = false)
    private String description;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "component_id", nullable = false)
    private ComponentPayload componentPayload;

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

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "assignableRole"
    )
    @Builder.Default
    private List<AssignableRoleCondition> conditions = new ArrayList<>();
}
