package dev.sheldan.abstracto.assignableroles.models.database;

import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.ARole;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="assignable_role")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AssignableRole implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "emote_id")
    private AEmote emote;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private ARole role;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @JoinColumn(name = "assignable_place_id", nullable = false)
    private AssignableRolePlace assignablePlace;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "place_post_id")
    private AssignableRolePlacePost assignableRolePlacePost;

    @Getter
    @Setter
    @ManyToMany(mappedBy = "roles")
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AssignedRoleUser> assignedUsers = new ArrayList<>();

    @Column(name = "description")
    private String description;

    @Column(name = "required_level")
    private Integer requiredLevel;

    @Column(name = "position")
    private Integer position;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
