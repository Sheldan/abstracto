package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.CounterId;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "counter")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Counter implements Serializable {

    @EmbeddedId
    private CounterId counterId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("serverId")
    @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
    private AServer server;

    @Column(name = "counter")
    private Long counter;
}
