package dev.sheldan.abstracto.assignableroles.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link AUserInAServer userInAServer} which added a reaction to an {@link AssignableRolePlace place}.
 * This is required in order to guarantee the uniqueness (if desired) of {@link AssignableRole} per place.
 */
@Entity
@Table(name = "assigned_role_user")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AssignedRoleUser implements Serializable {

    /**
     * The ID of the associated {@link AUserInAServer userInAServer}
     */
    @Id
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private AUserInAServer user;

    /**
     * The {@link AssignableRole assignableRoles} this user has in the server
     */
    @ManyToMany
    @JoinTable(
            name = "assigned_role_in_user",
            joinColumns = @JoinColumn(name = "user_in_server_id"),
            inverseJoinColumns = @JoinColumn(name = "assigned_role_id"))
    @Builder.Default
    private List<AssignableRole> roles = new ArrayList<>();

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
