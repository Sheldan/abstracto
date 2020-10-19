package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name="starboard_post")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class StarboardPost implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poster", nullable = false)
    private AUserInAServer author;

    @Column
    private Long starboardMessageId;

    @Column
    private Long postMessageId;

    @ManyToOne
    @JoinColumn(name = "channelId", nullable = false)
    private AChannel starboardChannel;

    @ManyToOne
    @JoinColumn(name = "sourceChannelId", nullable = false)
    private AChannel sourceChanel;

    @Transient
    private Integer reactionCount;

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

    @PostLoad
    private void onLoad() {
        this.reactionCount = this.reactions.size();
    }

    @Getter
    @OneToMany(fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade =  {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            mappedBy = "starboardPost")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<StarboardPostReaction> reactions;

    @Column
    private Instant starredDate;

    @Column
    private boolean ignored;

    public int getReactionCount() {
        if(this.reactions == null) {
            return 0;
        }
        return this.reactions.size();
    }

}
