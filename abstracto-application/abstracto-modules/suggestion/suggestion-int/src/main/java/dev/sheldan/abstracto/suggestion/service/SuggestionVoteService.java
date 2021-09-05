package dev.sheldan.abstracto.suggestion.service;

import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionDecision;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionVote;
import net.dv8tion.jda.api.entities.Member;

public interface SuggestionVoteService {
    SuggestionVote upsertSuggestionVote(Member votingMember, SuggestionDecision decision, Long suggestionId);
    SuggestionVote upsertSuggestionVote(Member votingMember, SuggestionDecision decision, Suggestion suggestion);
    void deleteSuggestionVote(Member votingMember, Long suggestionId);
    void deleteSuggestionVote(Member votingMember, Suggestion suggestion);
}
