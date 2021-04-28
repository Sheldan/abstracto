package dev.sheldan.abstracto.suggestion.repository;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;


@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, ServerSpecificId> {
    List<Suggestion> findByUpdatedLessThanAndStateNot(Instant start, SuggestionState state);
}
