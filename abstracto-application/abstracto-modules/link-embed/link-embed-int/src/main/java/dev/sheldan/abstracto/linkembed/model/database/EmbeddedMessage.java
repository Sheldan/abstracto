package dev.sheldan.abstracto.linkembed.model.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;

import jakarta.persistence.*;
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
public class EmbeddedMessage implements Serializable {

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embedded_user_in_server_id", nullable = false)
    private AUserInAServer embeddedUser;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embedding_user_in_server_id", nullable = false)
    private AUserInAServer embeddingUser;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embedded_server_id", nullable = false)
    private AServer embeddedServer;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embedded_channel_id", nullable = false)
    private AChannel embeddedChannel;

    @Column(name = "embedded_message_id", nullable = false)
    private Long embeddedMessageId;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embedding_server_id", nullable = false)
    private AServer embeddingServer;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "embedding_channel_id", nullable = false)
    private AChannel embeddingChannel;

    @Column(name = "embedding_message_id", nullable = false)
    @Id
    private Long embeddingMessageId;

    @Column(name = "deletion_component_id", length = 100)
    private String deletionComponentId;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
