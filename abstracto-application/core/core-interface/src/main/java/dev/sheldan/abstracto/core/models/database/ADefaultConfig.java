package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="default_configs")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ADefaultConfig implements Serializable {
    @javax.persistence.Id
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
}
