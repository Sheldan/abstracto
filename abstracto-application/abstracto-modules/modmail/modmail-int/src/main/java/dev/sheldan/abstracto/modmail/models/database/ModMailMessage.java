package dev.sheldan.abstracto.modmail.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;

/**
 * A table used to store which messages need to be retrieved when logging a mod mail thread.
 * These messages are only the messages passed between the member and the staff handling the thread and include all messages by the user
 * and only the messages send via command (reply/anonreply)
 */
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

    /**
     * The globally unique message ID which was send in the mod mail thread, either by a user of by the staff handling the thread
     */
    @Id
    private Long messageId;

    /**
     * The {@link AUserInAServer} which authored this message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modmail_message_author", nullable = false)
    private AUserInAServer author;

    /**
     * The {@link ModMailThread} in whose context this message was sent and this message is related to
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "threadReference", nullable = false)
    private ModMailThread threadReference;

    /**
     * Whether or not this message was from the user or a staff member, for convenience
     */
    private Boolean dmChannel;

    /**
     * Staff only: Whether or not this message meant to be sent anonymous
     */
    private Boolean anonymous;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }
}
