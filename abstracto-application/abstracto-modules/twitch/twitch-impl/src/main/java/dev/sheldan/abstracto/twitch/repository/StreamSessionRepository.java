package dev.sheldan.abstracto.twitch.repository;

import dev.sheldan.abstracto.twitch.model.database.StreamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StreamSessionRepository extends JpaRepository<StreamSession, Long> {
}
