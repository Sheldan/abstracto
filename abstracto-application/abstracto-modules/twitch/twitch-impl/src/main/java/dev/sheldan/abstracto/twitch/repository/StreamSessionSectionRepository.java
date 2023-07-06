package dev.sheldan.abstracto.twitch.repository;

import dev.sheldan.abstracto.twitch.model.database.StreamSessionSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StreamSessionSectionRepository extends JpaRepository<StreamSessionSection, Long> {
}
