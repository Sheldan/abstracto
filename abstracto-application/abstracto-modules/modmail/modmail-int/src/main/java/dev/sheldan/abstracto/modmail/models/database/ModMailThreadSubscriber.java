package dev.sheldan.abstracto.modmail.models.database;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;

/**
 * This able contains the staff users which subscribed to a certain mod mail thread and will get notified of new messages
 * in a mod mail thread
 */
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mod_mail_subscriber")
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ModMailThreadSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long subscriberId;

    /**
     * The staff member which is subscribed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_in_server_id", nullable = false)
    private AUserInAServer subscriber;

    /**
     * The thread for which the member is subscribed to
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "mod_mail_thread_id", nullable = false)
    private ModMailThread threadReference;

    @Column(name = "created")
    private Instant created;

    @Column(name = "updated")
    private Instant updated;

}
