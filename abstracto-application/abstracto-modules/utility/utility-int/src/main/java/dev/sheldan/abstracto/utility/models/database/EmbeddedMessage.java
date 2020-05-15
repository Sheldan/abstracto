package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name="embedded_message")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EmbeddedMessage {

    @Getter
    @ManyToOne
    @JoinColumn(name = "embeddedUser", nullable = false)
    private AUserInAServer embeddedUser;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embeddingUser", nullable = false)
    private AUserInAServer embeddingUser;

    @Getter
    @ManyToOne
    @JoinColumn(name = "originalServer", nullable = false)
    private AServer embeddedServer;

    @Getter
    @ManyToOne
    @JoinColumn(name = "originalChannel", nullable = false)
    private AChannel embeddedChannel;

    @Column
    private Long embeddedMessageId;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embeddingServer", nullable = false)
    private AServer embeddingServer;

    @Getter
    @ManyToOne
    @JoinColumn(name = "embeddingChannel", nullable = false)
    private AChannel embeddingChannel;

    @Column
    @Id
    private Long embeddingMessageId;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmbeddedMessage that = (EmbeddedMessage) o;
        return Objects.equals(embeddedUser, that.embeddedUser) &&
                Objects.equals(embeddingUser, that.embeddingUser) &&
                Objects.equals(embeddedServer, that.embeddedServer) &&
                Objects.equals(embeddedChannel, that.embeddedChannel) &&
                Objects.equals(embeddedMessageId, that.embeddedMessageId) &&
                Objects.equals(embeddingServer, that.embeddingServer) &&
                Objects.equals(embeddingChannel, that.embeddingChannel) &&
                Objects.equals(embeddingMessageId, that.embeddingMessageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(embeddedUser, embeddingUser, embeddedServer, embeddedChannel, embeddedMessageId, embeddingServer, embeddingChannel, embeddingMessageId);
    }
}
