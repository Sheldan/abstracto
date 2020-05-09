package dev.sheldan.abstracto.modmail.repository;

import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModMailMessageRepository extends JpaRepository<ModMailMessage, Long> {
    List<ModMailMessage> findByThreadReference(ModMailThread modMailThread);
}