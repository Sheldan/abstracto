package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.utility.models.template.commands.SuggestionLog;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

public interface SuggestionService {
    CompletableFuture<Void> createSuggestion(Member member, String text, SuggestionLog log);
    CompletableFuture<Void> acceptSuggestion(Long suggestionId, String text, SuggestionLog log);
    CompletableFuture<Void> rejectSuggestion(Long suggestionId, String text, SuggestionLog log);
}
