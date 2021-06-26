package dev.sheldan.abstracto.repostdetection.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.repostdetection.model.database.embed.PostIdentifier;
import lombok.*;

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
public class PostedImage {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_in_server_id", nullable = false)
    private AUserInAServer poster;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_channel_id", nullable = false)
    private AChannel postedChannel;

    @EmbeddedId
    @Getter
    @Setter
    private PostIdentifier postId;

    @Column(name = "image_hash", nullable = false)
    private String imageHash;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

    @Getter
    @OneToMany(fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade =  {CascadeType.PERSIST, CascadeType.MERGE},
            mappedBy = "originalPost")
    private List<Repost> reposts;

}
