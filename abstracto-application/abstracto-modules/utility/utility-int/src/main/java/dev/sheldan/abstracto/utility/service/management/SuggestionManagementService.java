package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.utility.models.database.Suggestion;
import dev.sheldan.abstracto.utility.models.SuggestionState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public interface SuggestionManagementService {
    Suggestion createSuggestion(Member suggester, String text);
    Suggestion createSuggestion(AUserInAServer suggester, String text);
    Suggestion getSuggestion(Long suggestionId);
    void setPostedMessage(Suggestion suggestion, Message message);
    void setSuggestionState(Suggestion suggestion, SuggestionState newState);
}
