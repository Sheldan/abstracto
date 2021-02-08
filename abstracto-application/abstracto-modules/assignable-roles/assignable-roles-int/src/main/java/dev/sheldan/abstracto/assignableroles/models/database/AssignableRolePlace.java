package dev.sheldan.abstracto.assignableroles.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="assignable_role_place")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AssignableRolePlace implements Serializable {

    public static final Long ASSIGNABLE_PLACE_NAME_LIMIT = 255L;
    public static final Long ASSIGNABLE_PLACE_DESCRIPTION_LIMIT = 255L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="channel_id")
    private AChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "key")
    private String key;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "assignablePlace"
    )
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AssignableRolePlacePost> messagePosts = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "assignablePlace"
    )
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AssignableRole> assignableRoles = new ArrayList<>();

    @Column(name = "text")
    private String text;

    @Builder.Default
    @Column(name = "active")
    private Boolean active = true;

    @Builder.Default
    @Column(name = "inline")
    private Boolean inline = false;

    @Builder.Default
    @Column(name = "unique_roles")
    private Boolean uniqueRoles = false;

    @Builder.Default
    @Column(name = "auto_remove")
    private Boolean autoRemove = false;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
