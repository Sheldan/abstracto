package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="default_config")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ADefaultConfig implements Serializable {
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column(name = "string_value")
    @Setter
    private String stringValue;

    @Column(name = "double_value")
    @Setter
    private Double doubleValue;

    @Column(name = "long_value")
    @Setter
    private Long longValue;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

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
}
