package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name="systemConfig")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AConfig implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

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

    public String getValueAsString() {
        if(getLongValue() != null) {
            return getLongValue().toString();
        } else if(getDoubleValue() != null) {
            return getDoubleValue().toString();
        } else if(getStringValue() != null) {
            return getStringValue();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AConfig config = (AConfig) o;
        return Objects.equals(id, config.id) &&
                Objects.equals(name, config.name) &&
                Objects.equals(stringValue, config.stringValue) &&
                Objects.equals(doubleValue, config.doubleValue) &&
                Objects.equals(server, config.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, stringValue, doubleValue, server);
    }
}
