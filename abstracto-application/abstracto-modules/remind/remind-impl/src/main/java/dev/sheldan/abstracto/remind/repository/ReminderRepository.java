package dev.sheldan.abstracto.remind.repository;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.remind.model.database.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> getByRemindedUserAndRemindedFalse(AUserInAServer aUserInAServer);

    Reminder getByIdAndRemindedUserAndRemindedFalse(Long reminderId, AUserInAServer aUserInAServer);

}
