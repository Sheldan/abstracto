package dev.sheldan.abstracto.suggestion.service;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

public interface SuggestionService {
    CompletableFuture<Void> createSuggestionMessage(Message commandMessage, String text);
    CompletableFuture<Void> acceptSuggestion(Long suggestionId, Message commandMessage, String text);
    CompletableFuture<Void> vetoSuggestion(Long suggestionId, Message commandMessage, String text);
    CompletableFuture<Void> rejectSuggestion(Long suggestionId, Message commandMessage, String text);
    CompletableFuture<Void> removeSuggestion(Long suggestionId, Member member);
    void cleanUpSuggestions();
}
