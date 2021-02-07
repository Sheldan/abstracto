package dev.sheldan.abstracto.modmail.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
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
@Table(name = "mod_mail_message")
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ModMailMessage implements Serializable {

    /**
     * The ID of the message which caused this message to be created, either the message containing the command or the message received from the user
     */
    @Id
    @Column(name = "id")
    private Long messageId;

    /**
     * The message which got created:
     * for a message from the user, the messageId of the message in the thread
     * for a message from staff, the messageId of the message in the DM channel
     */
    @Column(name = "created_message_in_dm")
    private Long createdMessageInDM;
    @Column(name = "created_message_in_channel")
    private Long createdMessageInChannel;

    /**
     * The {@link AUserInAServer} which authored this message
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_in_server_id", nullable = false)
    private AUserInAServer author;

    /**
     * The {@link ModMailThread} in whose context this message was sent and this message is related to
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "thread_id", nullable = false)
    private ModMailThread threadReference;

    /**
     * true: message was send via command, false: message was send from the user
     * This is used to decide where to get the message from in case of logging, because the user might delete the message and we do not want to re-parse the command message
     */
    @Column(name = "dm_channel")
    private Boolean dmChannel;

    /**
     * Staff only: Whether or not this message meant to be sent anonymous
     */
    @Column(name = "anonymous")
    private Boolean anonymous;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;
}
