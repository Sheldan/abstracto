package dev.sheldan.abstracto.assignableroles.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="assignable_role_place_post")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AssignableRolePlacePost implements Serializable {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "channel_id")
    private AChannel usedChannel;

    @Column(name = "created")
    private Instant created;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Getter
    @Setter
    @JoinColumn(name = "assignable_place_id", nullable = false)
    private AssignableRolePlace assignablePlace;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "assignableRolePlacePost")
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AssignableRole> assignableRoles = new ArrayList<>();

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

}
