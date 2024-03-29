package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="system_config")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AConfig implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "string_value")
    private String stringValue;

    @Column(name = "double_value")
    private Double doubleValue;

    @Column(name = "long_value")
    private Long longValue;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
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
