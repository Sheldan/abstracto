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
@Table(name = "modmail_subscriber")
@Cacheable
@Getter
@Setter
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ModMailThreadSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subscriberId;

    /**
     * The staff member which is subscribed
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modmail_thread_subscriber", nullable = false)
    private AUserInAServer subscriber;

    /**
     * The thread for which the member is subscribed to
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "modMailThread", nullable = false)
    private ModMailThread threadReference;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

}
