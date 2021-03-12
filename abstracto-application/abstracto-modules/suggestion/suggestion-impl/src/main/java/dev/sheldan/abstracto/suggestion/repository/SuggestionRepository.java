package dev.sheldan.abstracto.suggestion.repository;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, ServerSpecificId> {
}
