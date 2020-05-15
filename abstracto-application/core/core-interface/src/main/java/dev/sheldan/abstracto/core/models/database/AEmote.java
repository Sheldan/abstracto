package dev.sheldan.abstracto.core.models.database;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "emote")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AEmote {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @Column
    private String name;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AEmote emote = (AEmote) o;
        return Objects.equals(Id, emote.Id) &&
                Objects.equals(name, emote.name) &&
                Objects.equals(emoteKey, emote.emoteKey) &&
                Objects.equals(emoteId, emote.emoteId) &&
                Objects.equals(animated, emote.animated) &&
                Objects.equals(custom, emote.custom) &&
                Objects.equals(serverRef, emote.serverRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, name, emoteKey, emoteId, animated, custom, serverRef);
    }
}
