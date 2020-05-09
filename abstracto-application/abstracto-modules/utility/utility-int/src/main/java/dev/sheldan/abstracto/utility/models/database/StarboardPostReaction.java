package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="starboard_post_reaction")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class StarboardPostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reactorId", nullable = false)
    private AUserInAServer reactor;

    @OneToOne
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private StarboardPost starboardPost;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StarboardPostReaction that = (StarboardPostReaction) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(reactor, that.reactor) &&
                Objects.equals(starboardPost, that.starboardPost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reactor, starboardPost);
    }
}
