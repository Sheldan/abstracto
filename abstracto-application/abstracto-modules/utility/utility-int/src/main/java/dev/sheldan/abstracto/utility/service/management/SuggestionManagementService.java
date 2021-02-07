package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.Optional;

public interface SuggestionManagementService {
    Suggestion createSuggestion(Member suggester, String text, Message message, Long suggestionId);
    Suggestion createSuggestion(AUserInAServer suggester, String text, Message message, Long suggestionId);
    Optional<Suggestion> getSuggestion(Long suggestionId, Long serverId);
    void setSuggestionState(Suggestion suggestion, SuggestionState newState);
}
