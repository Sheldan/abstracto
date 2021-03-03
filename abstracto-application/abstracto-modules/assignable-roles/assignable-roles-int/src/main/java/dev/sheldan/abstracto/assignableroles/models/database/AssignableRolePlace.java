package dev.sheldan.abstracto.assignableroles.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The place entity holding the {@link AssignableRole roles} and {@link AssignableRolePlacePost posts} together.
 * This is also the entity holding all the configuration for the place and is identified by a key as a String, which is unique
 * for each server. This place holds the {@link AChannel} in which the
 */
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
    @Column(name = "id")
    private Long id;

    /**
     * The channel in which the {@link AssignableRolePlacePost posts} for this place should be created
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="channel_id")
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
    @Column(name = "key")
    private String key;

    /**
     * The {@link AssignableRolePlacePost posts} which were created when this place was setup. Is empty in the beginning
     * and actively maintained in case a post is deleted.
     */
    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "assignablePlace"
    )
    @Builder.Default
    private List<AssignableRolePlacePost> messagePosts = new ArrayList<>();

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

    /**
     * The text which is displayed in the first description area of the created {@link AssignableRolePlacePost}
     */
    @Column(name = "text")
    private String text;

    /**
     * Whether or not the reactions placed onto the posts should be acted upon
     */
    @Builder.Default
    @Column(name = "active")
    private Boolean active = true;

    /**
     * Whether or not the fields containing the descriptions should be inline
     */
    @Builder.Default
    @Column(name = "inline")
    private Boolean inline = false;

    /**
     * Whether or not it should be restricted, that a {@link AssignedRoleUser} should only have one role of this place
     */
    @Builder.Default
    @Column(name = "unique_roles")
    private Boolean uniqueRoles = false;

    /**
     * Whether or not the added reactions should be removed automatically
     */
    @Builder.Default
    @Column(name = "auto_remove")
    private Boolean autoRemove = false;

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
