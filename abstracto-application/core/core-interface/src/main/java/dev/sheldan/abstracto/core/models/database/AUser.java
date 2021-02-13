package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="auser")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class AUser implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "serverReference")
    private List<AUserInAServer> servers;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
