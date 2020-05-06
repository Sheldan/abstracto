package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="starboard_post")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class StarboardPost {

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

    @PostLoad
    private void onLoad() {
        this.reactionCount = this.reactions.size();
    }

    @Getter
    @OneToMany(fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade =  {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name="postId")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StarboardPost post = (StarboardPost) o;
        return ignored == post.ignored &&
                Objects.equals(id, post.id) &&
                Objects.equals(author, post.author) &&
                Objects.equals(starboardMessageId, post.starboardMessageId) &&
                Objects.equals(postMessageId, post.postMessageId) &&
                Objects.equals(starboardChannel, post.starboardChannel) &&
                Objects.equals(sourceChanel, post.sourceChanel) &&
                Objects.equals(reactionCount, post.reactionCount) &&
                Objects.equals(reactions, post.reactions) &&
                Objects.equals(starredDate, post.starredDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, author, starboardMessageId, postMessageId, starboardChannel, sourceChanel, reactionCount, reactions, starredDate, ignored);
    }
}
