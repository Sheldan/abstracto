package dev.sheldan.abstracto.remind.model.database.embed;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ReminderUserId implements Serializable {
    @Column(name = "reminder_id")
    private Long reminderId;
    @Column(name = "reminder_participant_user_in_server_id")
    private Long participantUserInServerId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReminderUserId counterId = (ReminderUserId) o;
        return Objects.equals(reminderId, counterId.reminderId) &&
                Objects.equals(participantUserInServerId, counterId.participantUserInServerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reminderId, participantUserInServerId);
    }

    public ReminderUserId(Long reminderId, Long participantUserInServerId) {
        this.reminderId = reminderId;
        this.participantUserInServerId = participantUserInServerId;
    }

    public ReminderUserId() {
    }
}
