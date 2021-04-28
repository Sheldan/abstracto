package dev.sheldan.abstracto.suggestion.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.suggestion.config.SuggestionPostTarget;
import dev.sheldan.abstracto.suggestion.exception.UnSuggestNotPossibleException;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionState;
import dev.sheldan.abstracto.suggestion.model.template.SuggestionLog;
import dev.sheldan.abstracto.suggestion.service.management.SuggestionManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class SuggestionServiceBean implements SuggestionService {

    public static final String SUGGESTION_CREATION_TEMPLATE = "suggest_initial";
    public static final String SUGGESTION_UPDATE_TEMPLATE = "suggest_update";
    public static final String SUGGESTION_YES_EMOTE = "suggestionYes";
    public static final String SUGGESTION_NO_EMOTE = "suggestionNo";
    public static final String SUGGESTION_COUNTER_KEY = "suggestion";

    @Autowired
    private SuggestionManagementService suggestionManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private SuggestionServiceBean self;

    @Autowired
    private CounterService counterService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Value("${abstracto.feature.suggestion.removalMaxAge}")
    private Long removalMaxAgeSeconds;

    @Value("${abstracto.feature.suggestion.removalDays}")
    private Long autoRemovalMaxDays;

    @Override
    public CompletableFuture<Void> createSuggestionMessage(Message commandMessage, String text)  {
        Member suggester = commandMessage.getMember();
        Long serverId = suggester.getGuild().getIdLong();
        AServer server = serverManagementService.loadServer(serverId);
        AUserInAServer userSuggester = userInServerManagementService.loadOrCreateUser(suggester);
        Long newSuggestionId = counterService.getNextCounterValue(server, SUGGESTION_COUNTER_KEY);
        SuggestionLog model = SuggestionLog
                .builder()
                .suggestionId(newSuggestionId)
                .state(SuggestionState.NEW)
                .serverId(serverId)
                .message(commandMessage)
                .member(commandMessage.getMember())
                .suggesterUser(userSuggester)
                .suggester(suggester.getUser())
                .text(text)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_CREATION_TEMPLATE, model, serverId);
        log.info("Creating suggestion with id {} in server {} from member {}.", newSuggestionId, serverId, suggester.getIdLong());
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION, serverId);
        return FutureUtils.toSingleFutureGeneric(completableFutures).thenCompose(aVoid -> {
            Message message = completableFutures.get(0).join();
            log.debug("Posted message, adding reaction for suggestion {} to message {}.", newSuggestionId, message.getId());
            CompletableFuture<Void> firstReaction = reactionService.addReactionToMessageAsync(SUGGESTION_YES_EMOTE, serverId, message);
            CompletableFuture<Void> secondReaction = reactionService.addReactionToMessageAsync(SUGGESTION_NO_EMOTE, serverId, message);
            return CompletableFuture.allOf(firstReaction, secondReaction).thenAccept(aVoid1 -> {
                log.debug("Reaction added to message {} for suggestion {}.", message.getId(), newSuggestionId);
                self.persistSuggestionInDatabase(suggester, text, message, newSuggestionId, commandMessage);
            });
        });
    }

    @Transactional
    public void persistSuggestionInDatabase(Member member, String text, Message message, Long suggestionId, Message commandMessage) {
        log.info("Persisting suggestion {} for server {} in database.", suggestionId, member.getGuild().getId());
        suggestionManagementService.createSuggestion(member, text, message, suggestionId, commandMessage);
    }

    @Override
    public CompletableFuture<Void> acceptSuggestion(Long suggestionId, Message commandMessage, String text) {
        Long serverId = commandMessage.getGuild().getIdLong();
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId);
        suggestionManagementService.setSuggestionState(suggestion, SuggestionState.ACCEPTED);
        log.info("Accepting suggestion {} in server {}.", suggestionId, suggestion.getServer().getId());
        return updateSuggestion(commandMessage.getMember(), text, suggestion);
    }

    @Override
    public CompletableFuture<Void> vetoSuggestion(Long suggestionId, Message commandMessage, String text) {
        Long serverId = commandMessage.getGuild().getIdLong();
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId);
        suggestionManagementService.setSuggestionState(suggestion, SuggestionState.VETOED);
        log.info("Vetoing suggestion {} in server {}.", suggestionId, suggestion.getServer().getId());
        return updateSuggestion(commandMessage.getMember(), text, suggestion);
    }

    private CompletableFuture<Void> updateSuggestion(Member memberExecutingCommand, String reason, Suggestion suggestion) {
        Long serverId = suggestion.getServer().getId();
        Long channelId = suggestion.getChannel().getId();
        Long originalMessageId = suggestion.getMessageId();
        SuggestionLog model = SuggestionLog
                .builder()
                .suggestionId(suggestion.getSuggestionId().getId())
                .state(suggestion.getState())
                .suggesterUser(suggestion.getSuggester())
                .serverId(serverId)
                .member(memberExecutingCommand)
                .originalMessageId(originalMessageId)
                .text(suggestion.getSuggestionText())
                .originalChannelId(channelId)
                .reason(reason)
                .build();
        log.info("Updated posted suggestion {} in server {}.", suggestion.getSuggestionId().getId(), suggestion.getServer().getId());
        CompletableFuture<User> memberById = userService.retrieveUserForId(suggestion.getSuggester().getUserReference().getId());
        CompletableFuture<Void> finalFuture = new CompletableFuture<>();
        memberById.whenComplete((user, throwable) -> {
            if(throwable == null) {
                model.setSuggester(user);
            }
            self.updateSuggestionMessageText(reason, model).thenAccept(unused -> finalFuture.complete(null)).exceptionally(throwable1 -> {
                finalFuture.completeExceptionally(throwable1);
                return null;
            });
        }).exceptionally(throwable -> {
            finalFuture.completeExceptionally(throwable);
            return null;
        });

        return finalFuture;

    }

    @Transactional
    public CompletableFuture<Void> updateSuggestionMessageText(String text, SuggestionLog suggestionLog)  {
        suggestionLog.setReason(text);
        Long serverId = suggestionLog.getServerId();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_UPDATE_TEMPLATE, suggestionLog, serverId);
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION, serverId);
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Void> rejectSuggestion(Long suggestionId, Message commandMessage, String text) {
        Long serverId = commandMessage.getGuild().getIdLong();
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId);
        suggestionManagementService.setSuggestionState(suggestion, SuggestionState.REJECTED);
        log.info("Rejecting suggestion {} in server {}.", suggestionId, suggestion.getServer().getId());
        return updateSuggestion(commandMessage.getMember(), text, suggestion);
    }

    @Override
    public CompletableFuture<Void> removeSuggestion(Long suggestionId, Member member) {
        Long serverId = member.getGuild().getIdLong();
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId);
        if(member.getIdLong() != suggestion.getSuggester().getUserReference().getId() ||
                suggestion.getCreated().isBefore(Instant.now().minus(Duration.ofSeconds(removalMaxAgeSeconds)))) {
            throw new UnSuggestNotPossibleException();
        }
        return messageService.deleteMessageInChannelInServer(suggestion.getServer().getId(), suggestion.getChannel().getId(), suggestion.getMessageId())
                .thenAccept(unused -> self.deleteSuggestion(suggestionId, serverId));
    }

    @Override
    @Transactional
    public void cleanUpSuggestions() {
        Instant pointInTime = Instant.now().minus(Duration.ofDays(autoRemovalMaxDays)).truncatedTo(ChronoUnit.DAYS);
        List<Suggestion> suggestionsToRemove = suggestionManagementService.getSuggestionsUpdatedBeforeNotNew(pointInTime);
        log.info("Removing {} suggestions older than {}.", suggestionsToRemove.size(), pointInTime);
        suggestionsToRemove.forEach(suggestion -> log.info("Deleting suggestion {} in server {}.",
                suggestion.getSuggestionId().getId(), suggestion.getSuggestionId().getServerId()));
        suggestionManagementService.deleteSuggestion(suggestionsToRemove);
    }

    @Transactional
    public void deleteSuggestion(Long suggestionId, Long serverId) {
        suggestionManagementService.deleteSuggestion(serverId, suggestionId);
    }
}
