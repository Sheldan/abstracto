package dev.sheldan.abstracto.suggestion.service;

import dev.sheldan.abstracto.suggestion.model.template.SuggestionLog;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

public interface SuggestionService {
    CompletableFuture<Void> createSuggestionMessage(Member member, String text, SuggestionLog log);
    CompletableFuture<Void> acceptSuggestion(Long suggestionId, String text, SuggestionLog log);
    CompletableFuture<Void> rejectSuggestion(Long suggestionId, String text, SuggestionLog log);
}
