package dev.sheldan.abstracto.moderation.repository;

import dev.sheldan.abstracto.moderation.model.database.ModerationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerationUserRepository extends JpaRepository<ModerationUser, Long> {
}
