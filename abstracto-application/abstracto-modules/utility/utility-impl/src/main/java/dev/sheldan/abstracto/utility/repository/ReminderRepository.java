package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.Reminder;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> getByRemindedUserAndRemindedFalse(AUserInAServer aUserInAServer);

    Reminder getByIdAndRemindedUserAndRemindedFalse(Long reminderId, AUserInAServer aUserInAServer);

    @NotNull
    @Override
    Optional<Reminder> findById(@NonNull Long aLong);
}
