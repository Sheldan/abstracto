package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.Fakeable;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "emote")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AEmote implements Serializable, Fakeable {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    // the way discord calls them and the unicode char for default Tweemoji emotes
    @Column
    @Setter
    private String emoteKey;

    @Column
    @Setter
    private Long emoteId;

    @Column
    @Setter
    private Boolean animated;

    @Column
    @Setter
    private Boolean custom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emote_server_id", nullable = false)
    private AServer serverRef;

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

    @Column(name = "changeable")
    @Getter
    @Setter
    @Builder.Default
    private boolean changeable = true;

    @Transient
    private boolean fake;

}
