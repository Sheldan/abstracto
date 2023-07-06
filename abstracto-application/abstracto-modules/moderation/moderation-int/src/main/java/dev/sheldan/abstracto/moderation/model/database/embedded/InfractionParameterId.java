package dev.sheldan.abstracto.moderation.model.database.embedded;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class InfractionParameterId implements Serializable {
    @Column(name = "infraction_id")
    private Long infractionId;
    @Column(name = "key")
    private String name;
}
