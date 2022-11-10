package dev.sheldan.abstracto.suggestion.service;

import dev.sheldan.abstracto.core.models.ServerChannelMessageUser;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.template.SuggestionInfoModel;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;

public interface SuggestionService {
    String SUGGESTION_REMINDER_DAYS_CONFIG_KEY = "suggestionReminderDays";
    String SUGGESTION_AUTO_EVALUATE_DAYS_CONFIG_KEY = "suggestionAutoEvaluateDays";
    String SUGGESTION_AUTO_EVALUATE_PERCENTAGE_CONFIG_KEY = "suggestionAutoEvaluatePercentage";
    CompletableFuture<Void> createSuggestionMessage(ServerChannelMessageUser cause, String text, String attachmentURL);
    CompletableFuture<Void> acceptSuggestion(Long serverId, Long suggestionId, Member actingMember, String text);
    CompletableFuture<Void> evaluateSuggestion(Long serverId, Long suggestionId);
    CompletableFuture<Void> vetoSuggestion(Long serverId, Long suggestionId, Member actingMember, String text);
    CompletableFuture<Void> rejectSuggestion(Long serverId, Long suggestionId, Member actingMember, String text);
    CompletableFuture<Void> removeSuggestion(Long serverId, Long suggestionId, Member member);
    void cleanUpSuggestions();
    CompletableFuture<Void> remindAboutSuggestion(ServerSpecificId suggestionId);
    void cancelSuggestionJobs(Suggestion suggestion);
    SuggestionInfoModel getSuggestionInfo(Long serverId, Long suggestionId);
    SuggestionInfoModel getSuggestionInfo(ServerSpecificId suggestionId);
}
