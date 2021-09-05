package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionDecision;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionVote;

import java.util.Optional;

public interface SuggestionVoteManagementService {
    Optional<SuggestionVote> getSuggestionVote(AUserInAServer aUserInAServer, Suggestion suggestion);
    void deleteSuggestionVote(AUserInAServer aUserInAServer, Suggestion suggestion);
    SuggestionVote createSuggestionVote(AUserInAServer aUserInAServer, Suggestion suggestion, SuggestionDecision decision);
    Long getDecisionsForSuggestion(Suggestion suggestion, SuggestionDecision decision);
    void deleteSuggestionVotes(Suggestion suggestion);
}
