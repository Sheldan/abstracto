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
}
