package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="posttarget")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PostTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    @Getter @Setter
    private AChannel channelReference;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="server_id", nullable = false)
    @Getter @Setter
    private AServer serverReference;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostTarget that = (PostTarget) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(channelReference, that.channelReference) &&
                Objects.equals(serverReference, that.serverReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, channelReference, serverReference);
    }
}
