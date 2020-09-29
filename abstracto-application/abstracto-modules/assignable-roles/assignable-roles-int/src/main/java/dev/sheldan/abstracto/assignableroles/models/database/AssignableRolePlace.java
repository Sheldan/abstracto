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
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AssignableRolePlace implements Serializable {

    public static final Long ASSIGNABLE_PLACE_NAME_LIMIT = 255L;
    public static final Long ASSIGNABLE_PLACE_DESCRIPTION_LIMIT = 255L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="channel_id")
    private AChannel channel;

    @OneToOne
    @JoinColumn(name="server_id")
    private AServer server;

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

    private String text;

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Boolean inline = false;

    @Builder.Default
    private Boolean uniqueRoles = false;

    @Builder.Default
    private Boolean autoRemove = false;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    @Column(name = "updated")
    private Instant updated;

    @PreUpdate
    private void onUpdate() {
        this.updated = Instant.now();
    }

}
