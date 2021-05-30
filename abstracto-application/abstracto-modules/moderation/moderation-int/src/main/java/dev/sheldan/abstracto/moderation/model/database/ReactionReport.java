package dev.sheldan.abstracto.moderation.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reaction_report")
@Getter
@Setter
@EqualsAndHashCode
public class ReactionReport {

    @Id
    @Column(name = "id")
    private Long reportMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_channel_id")
    private AChannel reportChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_in_server_id", nullable = false)
    private AUserInAServer reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_channel_id")
    private AChannel reportedChannel;

    @Column(name = "reported_message_id")
    private Long reportedMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private AServer server;

    @Column(name = "report_count")
    private Integer reportCount;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
