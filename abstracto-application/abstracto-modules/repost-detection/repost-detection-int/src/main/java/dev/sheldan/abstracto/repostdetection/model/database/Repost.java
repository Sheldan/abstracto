package dev.sheldan.abstracto.repostdetection.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.repostdetection.model.database.embed.RepostIdentifier;
import lombok.*;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "repost")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Repost {

    @EmbeddedId
    @Getter
    @Setter
    private RepostIdentifier repostId;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userInServerId")
    @JoinColumn(name = "user_in_server_id", referencedColumnName = "user_in_server_id", nullable = false)
    private AUserInAServer poster;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "count")
    private Integer count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
            {
                    @JoinColumn(updatable = false, insertable = false, name = "message_id", referencedColumnName = "message_id"),
                    @JoinColumn(updatable = false, insertable = false, name = "position", referencedColumnName = "position")
            })
    private PostedImage originalPost;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
