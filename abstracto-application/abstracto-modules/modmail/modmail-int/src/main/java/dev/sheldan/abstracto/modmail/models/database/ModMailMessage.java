package dev.sheldan.abstracto.modmail.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "modmail_messages")
@Cacheable
@Getter
@Setter
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ModMailMessage {

    @Id
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modmail_message_author", nullable = false)
    private AUserInAServer author;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "threadReference", nullable = false)
    private ModMailThread threadReference;

    private Boolean dmChannel;

    private Boolean anonymous;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }
}
