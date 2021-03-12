package dev.sheldan.abstracto.assignableroles.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * One individual {@link net.dv8tion.jda.api.entities.Message message} which was sent when setting up an {@link AssignableRolePlace place}
 * and contains the embeds and the reactions were placed onto it.
 */
@Entity
@Table(name = "assignable_role_place_post")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AssignableRolePlacePost implements Serializable {

    /**
     * The ID of the {@link net.dv8tion.jda.api.entities.Message message} which represents this post with the reactions.
     */
    @Id
    @Column(name = "id")
    private Long id;

    /**
     * The actual {@link AChannel channel} in which the post ended up in
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private AChannel usedChannel;

    /**
     * The {@link AServer server} in which this place post is posted
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    /**
     * The {@link AssignableRolePlace place} this post is associated with
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "assignable_place_id", nullable = false)
    private AssignableRolePlace assignablePlace;

    /**
     * The actual {@link AssignableRole assignableRoles} which are associated with this post, and whose respective {@link dev.sheldan.abstracto.core.models.database.AEmote}
     * have been placed as reactions
     */
    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "assignableRolePlacePost")
    @Builder.Default
    private List<AssignableRole> assignableRoles = new ArrayList<>();

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
