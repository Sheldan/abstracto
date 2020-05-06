package dev.sheldan.abstracto.modmail.repository;

import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModMailMessageRepository extends JpaRepository<ModMailMessage, Long> {
}
