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
    private Long id;

    @Getter
    @ManyToOne
    @JoinColumn(name = "remindedUser", nullable = false)
    private AUserInAServer remindedUser;

    @Getter
    private Long messageId;

    @Getter
    @ManyToOne
    @JoinColumn(name = "channelId", nullable = false)
    private AChannel channel;

    @Getter
    @ManyToOne
    @JoinColumn(name = "serverId", nullable = false)
    private AServer server;

    @Column(name = "created")
    private Instant reminderDate;

    @PrePersist
    private void onInsert() {
        this.reminderDate = Instant.now();
    }

    @Column(name = "updated")
    private Instant updated;

    @PreUpdate
    private void onUpdate() {
        this.updated = Instant.now();
    }

    @Getter
    private Instant targetDate;

    @Getter
    private String text;

    @Getter
    private boolean reminded;

    @Getter
    @Setter
    private String jobTriggerKey;

}
