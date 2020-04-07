package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.moderation.models.database.Warning;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarnRepository extends JpaRepository<Warning, Long> {
}
