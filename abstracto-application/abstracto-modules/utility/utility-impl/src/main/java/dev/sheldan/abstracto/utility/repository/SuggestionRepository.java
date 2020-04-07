package dev.sheldan.abstracto.utility.repository;

import dev.sheldan.abstracto.utility.models.database.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
}
