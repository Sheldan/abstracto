package dev.sheldan.abstracto.assignableroles.models.database;

import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import javax.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The {@link AEmote emote} this role is associated with
     */
    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "emote_id")
    private AEmote emote;

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
     * The {@link AssignableRolePlacePost} this assignable role is currently available as a reaction.
     * This is necessary, to easier find the reaction which are valid, in case a reaction is added to a post
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "place_post_id")
    private AssignableRolePlacePost assignableRolePlacePost;

    /**
     * The {@link AssignedRoleUser users} which currently have this role assigned via this mechanism.
     * This is necessary to enforce the unique property of {@link AssignableRolePlace}, in which you only may chose one
     * role.
     */
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    private List<AssignedRoleUser> assignedUsers = new ArrayList<>();

    /**
     * The description which is shown in the embeds of the posts of the {@link AssignableRolePlace}
     */
    @Column(name = "description")
    private String description;

    /**
     * The level in experience which is required in order to receive this {@link AssignableRole}
     */
    @Column(name = "required_level")
    private Integer requiredLevel;

    /**
     * The position of this assignable role within the {@link AssignableRole}. This is required in order to show them
     * the same order as the descriptions in the fields and also to move them around and switch positions
     */
    @Column(name = "position")
    private Integer position;

    /**
     * The {@link Instant} this entity was created
     */
    @Column(name = "created")
    private Instant created;

    /**
     * The {@link Instant} this entity was updated
     */
    @Column(name = "updated")
    private Instant updated;
}
