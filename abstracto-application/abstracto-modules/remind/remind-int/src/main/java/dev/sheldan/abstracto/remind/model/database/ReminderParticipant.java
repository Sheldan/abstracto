package dev.sheldan.abstracto.remind.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.remind.model.database.embed.ReminderUserId;
import lombok.*;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name="reminder_participant")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ReminderParticipant {

    @EmbeddedId
    private ReminderUserId reminderParticipantId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("reminderId")
    @JoinColumn(name = "reminder_id", nullable = false)
    private Reminder reminder;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @MapsId("participantUserInServerId")
    @JoinColumn(name = "reminder_participant_user_in_server_id", nullable = false)
    private AUserInAServer participator;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private AServer server;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;
}
