package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="role")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ARole implements SnowFlake, Serializable {

    @Id
    @Getter
    @Setter
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Getter
    @Setter
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @Transient
    private boolean fake;

    public String getAsMention() {
        return "<@&" + getId() + '>';
    }
}
