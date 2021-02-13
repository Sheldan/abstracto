package dev.sheldan.abstracto.assignableroles.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="assigned_role_user")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AssignedRoleUser implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @PrimaryKeyJoinColumn
    private AUserInAServer user;

    @ManyToMany
    @JoinTable(
            name = "assigned_role_in_user",
            joinColumns = @JoinColumn(name = "user_in_server_id"),
            inverseJoinColumns = @JoinColumn(name = "assigned_role_id"))
    @Builder.Default
    private List<AssignableRole> roles = new ArrayList<>();

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
