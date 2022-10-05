package dev.sheldan.abstracto.remind.repository;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import dev.sheldan.abstracto.remind.model.database.ReminderParticipant;
import dev.sheldan.abstracto.remind.model.database.embed.ReminderUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderParticipantRepository extends JpaRepository<ReminderParticipant, ReminderUserId> {
    List<ReminderParticipant> findAllByReminder(Reminder reminder);
    List<ReminderParticipant> findAllByParticipant(AUserInAServer aUserInAServer);
    List<ReminderParticipant> findAllByParticipantAndReminder_RemindedFalse(AUserInAServer aUserInAServer);
}
