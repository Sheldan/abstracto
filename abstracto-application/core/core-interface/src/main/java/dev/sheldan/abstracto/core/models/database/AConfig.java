package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name="systemConfig")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @Column
    private String name;

    @Column
    @Setter
    private String stringValue;

    @Column
    @Setter
    private Double doubleValue;

    @Column
    @Setter
    private Long longValue;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    @Getter
    @Setter
    private AServer server;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AConfig config = (AConfig) o;
        return Objects.equals(Id, config.Id) &&
                Objects.equals(name, config.name) &&
                Objects.equals(stringValue, config.stringValue) &&
                Objects.equals(doubleValue, config.doubleValue) &&
                Objects.equals(server, config.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, name, stringValue, doubleValue, server);
    }
}
