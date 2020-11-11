package dev.sheldan.abstracto.statistic.emotes.model.database;

import dev.sheldan.abstracto.core.models.Fakeable;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emotes.model.database.embed.TrackedEmoteServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tracked_emote")
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TrackedEmote implements Serializable, Fakeable {

    @EmbeddedId
    private TrackedEmoteServer trackedEmoteId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("serverId")
    @JoinColumn(name = "server_id",  referencedColumnName = "id", nullable = false)
    private AServer server;

    @Column(name = "name", length = 32)
    private String emoteName;

    @Column(name = "animated")
    private Boolean animated;

    @Column(name = "tracking_enabled")
    private Boolean trackingEnabled;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "external")
    private Boolean external;

    @Column(name = "external_url")
    private String externalUrl;

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

    @Transient
    private boolean fake;
}
