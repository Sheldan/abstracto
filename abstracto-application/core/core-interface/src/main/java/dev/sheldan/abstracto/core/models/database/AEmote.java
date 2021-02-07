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
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    // the way discord calls them and the unicode char for default Tweemoji emotes
    @Column(name = "emote_key")
    @Setter
    private String emoteKey;

    @Column(name = "emote_id")
    @Setter
    private Long emoteId;

    @Column(name = "animated")
    @Setter
    private Boolean animated;

    @Column(name = "custom")
    @Setter
    private Boolean custom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer serverRef;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

    @Column(name = "changeable")
    @Getter
    @Setter
    @Builder.Default
    private boolean changeable = true;

    @Transient
    private boolean fake;

}
