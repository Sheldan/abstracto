package dev.sheldan.abstracto.suggestion.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.Optional;

public interface SuggestionManagementService {
    Suggestion createSuggestion(Member suggester, String text, Message message, Long suggestionId);
    Suggestion createSuggestion(AUserInAServer suggester, String text, Message message, Long suggestionId);
    Optional<Suggestion> getSuggestion(Long suggestionId, Long serverId);
    void setSuggestionState(Suggestion suggestion, SuggestionState newState);
}
