package dev.sheldan.abstracto.suggestion.service;

import dev.sheldan.abstracto.core.models.ServerChannelMessageUser;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.template.SuggestionInfoModel;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

public interface SuggestionService {
    String SUGGESTION_REMINDER_DAYS_CONFIG_KEY = "suggestionReminderDays";
    CompletableFuture<Void> createSuggestionMessage(ServerChannelMessageUser cause, String text, String attachmentURL);
    CompletableFuture<Void> acceptSuggestion(Long suggestionId, Member actingMember, String text);
    CompletableFuture<Void> vetoSuggestion(Long suggestionId, Member actingMember, String text);
    CompletableFuture<Void> rejectSuggestion(Long suggestionId, Member actingMember, String text);
    CompletableFuture<Void> removeSuggestion(Long suggestionId, Member member);
    void cleanUpSuggestions();
    CompletableFuture<Void> remindAboutSuggestion(ServerSpecificId suggestionId);
    void cancelSuggestionReminder(Suggestion suggestion);
    SuggestionInfoModel getSuggestionInfo(Long serverId, Long suggestionId);
    SuggestionInfoModel getSuggestionInfo(ServerSpecificId suggestionId);
}
