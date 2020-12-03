package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.embed.PostIdentifier;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "posted_image")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class PostedImage {

    @Getter
    @ManyToOne
    @JoinColumn(name = "posting_user_id", nullable = false)
    private AUserInAServer poster;

    @Getter
    @ManyToOne
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Getter
    @ManyToOne
    @JoinColumn(name = "posted_channel_id", nullable = false)
    private AChannel postedChannel;

    @EmbeddedId
    @Getter
    @Setter
    private PostIdentifier postId;

    @Column(name = "image_hash")
    private String imageHash;

    @Column(name = "created")
    private Instant created;

    @Getter
    @OneToMany(fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade =  {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            mappedBy = "originalPost")
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Repost> reposts;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }
}
