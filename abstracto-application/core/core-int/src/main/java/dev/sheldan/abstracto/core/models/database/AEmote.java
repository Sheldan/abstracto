package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.models.Fakeable;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "emote")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class AEmote implements Serializable, Fakeable {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
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

    @Builder.Default
    @Column(name = "custom", nullable = false)
    @Setter
    private Boolean custom = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer serverRef;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @Column(name = "changeable")
    @Getter
    @Setter
    @Builder.Default
    private boolean changeable = true;

    @Transient
    private boolean fake;

}
