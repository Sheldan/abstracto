package dev.sheldan.abstracto.stickyroles.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sticky_role_user")
@Getter
@Setter
@EqualsAndHashCode
public class StickyRoleUser {
    /**
     * The ID of the {@link AUserInAServer user} which is represented by this object
     */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * The {@link AUserInAServer user} which is represented by this object
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private AUserInAServer user;

    @Column(name = "sticky")
    private Boolean sticky;

    @ManyToMany(mappedBy = "users")
    @Builder.Default
    private List<StickyRole> roles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
