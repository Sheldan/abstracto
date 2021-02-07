package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name="embedded_message")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EmbeddedMessage implements Serializable {

    @Getter
    @ManyToOne
    @JoinColumn(name = "embedded_user_in_server_id", nullable = false)
    private AUserInAServer embeddedUser;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embedding_user_in_server_id", nullable = false)
    private AUserInAServer embeddingUser;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embedded_server_id", nullable = false)
    private AServer embeddedServer;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embedded_channel_id", nullable = false)
    private AChannel embeddedChannel;

    @Column(name = "embedded_message_id")
    private Long embeddedMessageId;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embedding_server_id", nullable = false)
    private AServer embeddingServer;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embeddingChannel", nullable = false)
    private AChannel embeddingChannel;

    @Column(name = "embedding_message_id")
    @Id
    private Long embeddingMessageId;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
