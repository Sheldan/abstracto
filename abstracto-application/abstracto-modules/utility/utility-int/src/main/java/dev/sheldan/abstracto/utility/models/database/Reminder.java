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
@Table(name="reminder")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Reminder implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Column(name = "id")
    private Long id;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_in_server_id", nullable = false)
    private AUserInAServer remindedUser;

    @Getter
    @Column(name = "message_id")
    private Long messageId;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private AChannel channel;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "created")
    private Instant reminderDate;

    @Column(name = "updated")
    private Instant updated;

    @Getter
    @Column(name = "target_date")
    private Instant targetDate;

    @Getter
    @Column(name = "text")
    private String text;

    @Getter
    @Column(name = "reminded")
    private boolean reminded;

    @Getter
    @Setter
    @Column(name = "job_trigger_key")
    private String jobTriggerKey;

}
