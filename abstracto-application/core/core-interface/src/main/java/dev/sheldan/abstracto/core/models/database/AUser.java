package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AUser {

    @Id
    private Long id;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "serverReference")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<AUserInAServer> servers;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AUser user = (AUser) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(servers, user.servers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, servers);
    }
}
