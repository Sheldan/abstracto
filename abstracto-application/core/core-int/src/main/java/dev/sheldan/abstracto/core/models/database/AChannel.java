package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.SnowFlake;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="channel")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AChannel implements SnowFlake, Serializable {

    @Id
    @Getter
    @Column(name = "id", nullable = false)
    public Long id;

    @Getter
    @ManyToMany(mappedBy = "channels")
    private List<AChannelGroup> groups;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Getter
    @Enumerated(EnumType.STRING)
    private AChannelType type;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @Getter
    @Setter
    @Builder.Default
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Transient
    private boolean fake;


}
