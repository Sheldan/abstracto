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
@Table(name="reminder")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Reminder {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return reminded == reminder.reminded &&
                Objects.equals(id, reminder.id) &&
                Objects.equals(remindedUser, reminder.remindedUser) &&
                Objects.equals(messageId, reminder.messageId) &&
                Objects.equals(channel, reminder.channel) &&
                Objects.equals(server, reminder.server) &&
                Objects.equals(reminderDate, reminder.reminderDate) &&
                Objects.equals(targetDate, reminder.targetDate) &&
                Objects.equals(text, reminder.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, remindedUser, messageId, channel, server, reminderDate, targetDate, text, reminded);
    }
}
