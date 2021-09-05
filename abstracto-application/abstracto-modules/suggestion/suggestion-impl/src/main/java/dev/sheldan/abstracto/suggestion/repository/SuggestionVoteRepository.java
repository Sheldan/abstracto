package dev.sheldan.abstracto.suggestion.repository;

import dev.sheldan.abstracto.suggestion.model.database.SuggestionDecision;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionVote;
import dev.sheldan.abstracto.suggestion.model.database.embed.SuggestionVoterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuggestionVoteRepository extends JpaRepository<SuggestionVote, SuggestionVoterId> {
    Long countByDecisionAndSuggestionVoteId_SuggestionIdAndSuggestionVoteId_ServerId(SuggestionDecision decision, Long suggestionId, Long serverId);
    void deleteBySuggestionVoteId_SuggestionIdAndSuggestionVoteId_ServerId(Long suggestionId, Long serverId);
}
