package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SuggestionManagementService {
    Suggestion createSuggestion(Member suggester, String text, Message message, Long suggestionId, Message commandMessage);
    Suggestion createSuggestion(AUserInAServer suggester, String text, Message message, Long suggestionId, Message commandMessage);
    Optional<Suggestion> getSuggestionOptional(Long serverId, Long suggestionId);
    Suggestion getSuggestion(Long serverId, Long suggestionId);
    void setSuggestionState(Suggestion suggestion, SuggestionState newState);
    void deleteSuggestion(Long serverId, Long suggestionId);
    void deleteSuggestion(List<Suggestion> suggestions);
    void deleteSuggestion(Suggestion suggestion);
    List<Suggestion> getSuggestionsUpdatedBeforeNotNew(Instant date);
}
