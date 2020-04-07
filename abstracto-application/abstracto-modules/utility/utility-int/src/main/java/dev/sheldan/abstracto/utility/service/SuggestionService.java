package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.utility.models.template.commands.SuggestionLog;
import net.dv8tion.jda.api.entities.Member;

public interface SuggestionService {
    void createSuggestion(Member member, String text, SuggestionLog log);
    void acceptSuggestion(Long suggestionId, String text, SuggestionLog log);
    void rejectSuggestion(Long suggestionId, String text, SuggestionLog log);
    void validateSetup(Long serverId);
}
