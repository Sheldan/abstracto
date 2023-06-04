package dev.sheldan.abstracto.suggestion.repository;

import dev.sheldan.abstracto.suggestion.model.database.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
}
