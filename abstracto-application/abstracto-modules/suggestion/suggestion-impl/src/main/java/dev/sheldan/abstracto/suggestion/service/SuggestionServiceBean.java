package dev.sheldan.abstracto.suggestion.service;

import dev.sheldan.abstracto.core.interaction.ComponentService;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.ServerChannelMessageUser;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.interaction.button.ButtonConfigModel;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.service.SchedulerService;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureDefinition;
import dev.sheldan.abstracto.suggestion.config.SuggestionFeatureMode;
import dev.sheldan.abstracto.suggestion.config.SuggestionPostTarget;
import dev.sheldan.abstracto.suggestion.exception.UnSuggestNotPossibleException;
import dev.sheldan.abstracto.suggestion.model.database.Suggestion;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionDecision;
import dev.sheldan.abstracto.suggestion.model.database.SuggestionState;
import dev.sheldan.abstracto.suggestion.model.template.*;
import dev.sheldan.abstracto.suggestion.service.management.SuggestionManagementService;
import dev.sheldan.abstracto.suggestion.service.management.SuggestionVoteManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
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
    public static final String SUGGESTION_REMINDER_TEMPLATE_KEY = "suggest_suggestion_reminder";
    public static final String SUGGESTION_VOTE_ORIGIN = "suggestionVote";

    @Autowired
    private SuggestionManagementService suggestionManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

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

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private SuggestionVoteManagementService suggestionVoteManagementService;

    @Value("${abstracto.feature.suggestion.removalMaxAge}")
    private Long removalMaxAgeSeconds;

    @Value("${abstracto.feature.suggestion.removalDays}")
    private Long autoRemovalMaxDays;

    @Override
    public CompletableFuture<Void> createSuggestionMessage(ServerChannelMessageUser cause, String text, String attachmentURL)  {
        // it is done that way, because we cannot always be sure, that the message contains the member
        return memberService.getMemberInServerAsync(cause.getServerId(), cause.getUserId())
                .thenCompose(suggester -> self.createMessageWithSuggester(cause.getChannelId(), cause.getMessageId(), text, suggester, attachmentURL));
    }

    @Transactional
    public CompletableFuture<Void> createMessageWithSuggester(Long suggestionChannelId, Long suggestionMessageId, String text, Member suggester, String attachmentURL) {
        postTargetService.validatePostTarget(SuggestionPostTarget.SUGGESTION, suggester.getGuild().getIdLong());
        Long serverId = suggester.getGuild().getIdLong();
        AServer server = serverManagementService.loadServer(serverId);
        AUserInAServer userSuggester = userInServerManagementService.loadOrCreateUser(suggester);
        Long newSuggestionId = counterService.getNextCounterValue(server, SUGGESTION_COUNTER_KEY);
        Boolean useButtons = featureModeService.featureModeActive(SuggestionFeatureDefinition.SUGGEST, serverId, SuggestionFeatureMode.SUGGESTION_BUTTONS);
        Boolean autoEvaluationEnabled = featureModeService.featureModeActive(SuggestionFeatureDefinition.SUGGEST, serverId, SuggestionFeatureMode.SUGGESTION_AUTO_EVALUATE);
        Long autoEvaluateDays = null;
        if(autoEvaluationEnabled) {
            autoEvaluateDays = configService.getLongValueOrConfigDefault(SUGGESTION_AUTO_EVALUATE_DAYS_CONFIG_KEY, serverId);
        }
        SuggestionLog model = SuggestionLog
                .builder()
                .suggestionId(newSuggestionId)
                .state(SuggestionState.NEW)
                .serverId(serverId)
                .attachmentURL(attachmentURL)
                .member(suggester)
                .suggesterUser(userSuggester)
                .useButtons(useButtons)
                .suggester(suggester.getUser())
                .text(text)
                .autoEvaluationEnabled(autoEvaluationEnabled)
                .autoEvaluationTargetDate(autoEvaluationEnabled ? Instant.now().plus(autoEvaluateDays, ChronoUnit.DAYS) : null)
                .build();
        if(useButtons) {
            setupButtonIds(model);
        }
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_CREATION_TEMPLATE, model, serverId);
        log.info("Creating suggestion with id {} in server {} from member {}.", newSuggestionId, serverId, suggester.getIdLong());
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION, serverId);
        return FutureUtils.toSingleFutureGeneric(completableFutures)
                .thenCompose(aVoid -> self.addDeletionPossibility(suggestionChannelId, suggestionMessageId, text, suggester, serverId, newSuggestionId, completableFutures, model));
    }

    @Transactional
    public CompletableFuture<Void> addDeletionPossibility(Long suggestionChannelId, Long suggestionMessageId, String text, Member suggester, Long serverId,
                                                          Long newSuggestionId, List<CompletableFuture<Message>> completableFutures, SuggestionLog model) {
        Message message = completableFutures.get(0).join();
        if(model.getUseButtons()) {
            configureDecisionButtonPayload(serverId, newSuggestionId, model.getAgreeButtonModel(), SuggestionDecision.AGREE);
            configureDecisionButtonPayload(serverId, newSuggestionId, model.getDisAgreeButtonModel(), SuggestionDecision.DISAGREE);
            configureDecisionButtonPayload(serverId, newSuggestionId, model.getRemoveVoteButtonModel(), SuggestionDecision.REMOVE_VOTE);
            AServer server = serverManagementService.loadServer(serverId);
            componentPayloadManagementService.createButtonPayload(model.getAgreeButtonModel(), server);
            componentPayloadManagementService.createButtonPayload(model.getDisAgreeButtonModel(), server);
            componentPayloadManagementService.createButtonPayload(model.getRemoveVoteButtonModel(), server);
            self.persistSuggestionInDatabase(suggester, text, message, newSuggestionId, suggestionChannelId, suggestionMessageId);
            return CompletableFuture.completedFuture(null);
        } else {
            log.debug("Posted message, adding reaction for suggestion {} to message {}.", newSuggestionId, message.getId());
            CompletableFuture<Void> firstReaction = reactionService.addReactionToMessageAsync(SUGGESTION_YES_EMOTE, serverId, message);
            CompletableFuture<Void> secondReaction = reactionService.addReactionToMessageAsync(SUGGESTION_NO_EMOTE, serverId, message);
            return CompletableFuture.allOf(firstReaction, secondReaction).thenAccept(aVoid1 -> {
                log.debug("Reaction added to message {} for suggestion {}.", message.getId(), newSuggestionId);
                self.persistSuggestionInDatabase(suggester, text, message, newSuggestionId, suggestionChannelId, suggestionMessageId);
            });
        }
    }

    private void configureDecisionButtonPayload(Long serverId, Long newSuggestionId, ButtonConfigModel model, SuggestionDecision decision) {
        SuggestionButtonPayload agreePayload = SuggestionButtonPayload
                .builder()
                .suggestionId(newSuggestionId)
                .serverId(serverId)
                .decision(decision)
                .build();
        model.setButtonPayload(agreePayload);
        model.setOrigin(SUGGESTION_VOTE_ORIGIN);
        model.setPayloadType(SuggestionButtonPayload.class);
    }

    private void setupButtonIds(SuggestionLog suggestionLog) {
        suggestionLog.setAgreeButtonModel(componentService.createButtonConfigModel());
        suggestionLog.setDisAgreeButtonModel(componentService.createButtonConfigModel());
        suggestionLog.setRemoveVoteButtonModel(componentService.createButtonConfigModel());
    }

    @Transactional
    public void persistSuggestionInDatabase(Member member, String text, Message message, Long suggestionId, Long suggestionChannelId, Long suggestionMessageId) {
        Long serverId = member.getGuild().getIdLong();
        log.info("Persisting suggestion {} for server {} in database.", suggestionId, serverId);
        Suggestion suggestion = suggestionManagementService.createSuggestion(member, text, message, suggestionId, suggestionChannelId, suggestionMessageId);
        if(featureModeService.featureModeActive(SuggestionFeatureDefinition.SUGGEST, serverId, SuggestionFeatureMode.SUGGESTION_REMINDER)) {
            String triggerKey = scheduleSuggestionReminder(serverId, suggestionId);
            suggestion.setSuggestionReminderJobTriggerKey(triggerKey);
        }
        if(featureModeService.featureModeActive(SuggestionFeatureDefinition.SUGGEST, serverId, SuggestionFeatureMode.SUGGESTION_AUTO_EVALUATE)) {
            String triggerKey = scheduleEvaluationReminder(serverId, suggestionId);
            suggestion.setSuggestionEvaluationJobTriggerKey(triggerKey);
        }
    }

    private String scheduleSuggestionReminder(Long serverId, Long suggestionId) {
        HashMap<Object, Object> parameters = new HashMap<>();
        parameters.put("serverId", serverId.toString());
        parameters.put("suggestionId", suggestionId.toString());
        JobParameters jobParameters = JobParameters.builder().parameters(parameters).build();
        Long days = configService.getLongValueOrConfigDefault(SuggestionService.SUGGESTION_REMINDER_DAYS_CONFIG_KEY, serverId);
        Instant targetDate = Instant.now().plus(days, ChronoUnit.DAYS);
        String triggerKey = schedulerService.executeJobWithParametersOnce("suggestionReminderJob", "suggestion", jobParameters, Date.from(targetDate));
        log.info("Starting scheduled job  with trigger {} to execute suggestion reminder in server {} for suggestion {}.", triggerKey, serverId, suggestionId);
        return triggerKey;
    }

    private String scheduleEvaluationReminder(Long serverId, Long suggestionId) {
        HashMap<Object, Object> parameters = new HashMap<>();
        parameters.put("serverId", serverId.toString());
        parameters.put("suggestionId", suggestionId.toString());
        JobParameters jobParameters = JobParameters.builder().parameters(parameters).build();
        Long days = configService.getLongValueOrConfigDefault(SuggestionService.SUGGESTION_AUTO_EVALUATE_DAYS_CONFIG_KEY, serverId);
        Instant targetDate = Instant.now().plus(days, ChronoUnit.DAYS);
        String triggerKey = schedulerService.executeJobWithParametersOnce("suggestionEvaluationJob", "suggestion", jobParameters, Date.from(targetDate));
        log.info("Starting scheduled job  with trigger {} to execute suggestion evaluation in server {} for suggestion {}.", triggerKey, serverId, suggestionId);
        return triggerKey;
    }

    @Override
    public CompletableFuture<Void> acceptSuggestion(Long serverId, Long suggestionId, Member actingMember, String text) {
        return self.setSuggestionToFinalState(actingMember, serverId, suggestionId, text, SuggestionState.ACCEPTED);
    }

    @Override
    public CompletableFuture<Void> evaluateSuggestion(Long serverId, Long suggestionId) {
        Long approvalPercentage = configService.getLongValueOrConfigDefault(SUGGESTION_AUTO_EVALUATE_PERCENTAGE_CONFIG_KEY, serverId);
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId);
        Long agreements = suggestionVoteManagementService.getDecisionsForSuggestion(suggestion, SuggestionDecision.AGREE);
        Long disagreements = suggestionVoteManagementService.getDecisionsForSuggestion(suggestion, SuggestionDecision.DISAGREE);
        double suggestionPercentage = ((double) agreements) / (disagreements + agreements) * 100;
        if(suggestionPercentage > approvalPercentage) {
            return acceptSuggestion(serverId, suggestionId, null, null);
        } else {
            return rejectSuggestion(serverId, suggestionId, null, null);
        }
    }

    @Transactional
    public CompletableFuture<Void> setSuggestionToFinalState(Member executingMember, Long serverId, Long suggestionId, String text, SuggestionState state) {
        postTargetService.validatePostTarget(SuggestionPostTarget.SUGGESTION, serverId);
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId);
        suggestionManagementService.setSuggestionState(suggestion, state);
        cancelSuggestionJobs(suggestion);
        log.info("Setting suggestion {} in server {} to state {}", suggestionId, suggestion.getServer().getId(), state);
        return updateSuggestion(executingMember, text, suggestion);
    }

    @Override
    public CompletableFuture<Void> vetoSuggestion(Long serverId, Long suggestionId, Member actingMember, String text) {
        return self.setSuggestionToFinalState(actingMember, serverId, suggestionId, text, SuggestionState.VETOED);
    }

    private CompletableFuture<Void> updateSuggestion(Member memberExecutingCommand, String reason, Suggestion suggestion) {
        Long serverId = suggestion.getServer().getId();
        Long channelId = suggestion.getChannel().getId();
        Long originalMessageId = suggestion.getMessageId();
        Long agreements = suggestionVoteManagementService.getDecisionsForSuggestion(suggestion, SuggestionDecision.AGREE);
        Long disagreements = suggestionVoteManagementService.getDecisionsForSuggestion(suggestion, SuggestionDecision.DISAGREE);
        Long suggestionId = suggestion.getSuggestionId().getId();
        SuggestionUpdateModel model = SuggestionUpdateModel
                .builder()
                .suggestionId(suggestionId)
                .state(suggestion.getState())
                .serverId(serverId)
                .member(memberExecutingCommand)
                .agreeVotes(agreements)
                .disAgreeVotes(disagreements)
                .originalMessageId(originalMessageId)
                .text(suggestion.getSuggestionText())
                .originalChannelId(channelId)
                .reason(reason)
                .build();
        log.info("Updated posted suggestion {} in server {}.", suggestionId, suggestion.getServer().getId());
        CompletableFuture<User> memberById = userService.retrieveUserForId(suggestion.getSuggester().getUserReference().getId());
        CompletableFuture<Void> finalFuture = new CompletableFuture<>();
        memberById.whenComplete((user, throwable) -> {
            if(throwable == null) {
                model.setSuggester(user);
            }
            self.updateSuggestionMessageText(reason, model)
            .thenAccept(unused -> self.removeSuggestionButtons(serverId, channelId, originalMessageId, suggestionId))
            .thenAccept(unused -> finalFuture.complete(null))
            .exceptionally(throwable1 -> {
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
    public CompletableFuture<Void> removeSuggestionButtons(Long serverId, Long channelId, Long messageId, Long suggestionId) {
        Boolean useButtons = featureModeService.featureModeActive(SuggestionFeatureDefinition.SUGGEST, serverId, SuggestionFeatureMode.SUGGESTION_BUTTONS);
        if(useButtons) {
            return messageService.loadMessage(serverId, channelId, messageId).thenCompose(message -> {
                log.info("Clearing buttons from suggestion {} in with message {} in channel {} in server {}.", suggestionId, message, channelId, serverId);
                return componentService.clearButtons(message);
            });
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Transactional
    public CompletableFuture<Void> updateSuggestionMessageText(String text, SuggestionUpdateModel suggestionLog)  {
        suggestionLog.setReason(text);
        Long serverId = suggestionLog.getServerId();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_UPDATE_TEMPLATE, suggestionLog, serverId);
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION, serverId);
        return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
    }

    @Override
    public CompletableFuture<Void> rejectSuggestion(Long serverId, Long suggestionId, Member actingMember, String text) {
        return self.setSuggestionToFinalState(actingMember, serverId, suggestionId, text, SuggestionState.REJECTED);
    }

    @Override
    public CompletableFuture<Void> removeSuggestion(Long serverId, Long suggestionId, Member member) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId);
        if(member.getIdLong() != suggestion.getSuggester().getUserReference().getId() ||
                suggestion.getCreated().isBefore(Instant.now().minus(Duration.ofSeconds(removalMaxAgeSeconds)))) {
            throw new UnSuggestNotPossibleException();
        }
        return messageService.deleteMessageInChannelInServer(suggestion.getServer().getId(), suggestion.getChannel().getId(), suggestion.getMessageId())
                .thenAccept(unused -> self.deleteSuggestion(suggestionId, serverId))
                .exceptionally(throwable -> {
                    log.info("Suggestion message for suggestion {} in server {} did not exist anymore - ignoring.", suggestionId, serverId);
                    self.deleteSuggestion(suggestionId, serverId);
                    return null;
                });
    }

    @Override
    @Transactional
    public void cleanUpSuggestions() {
        Instant pointInTime = Instant.now().minus(Duration.ofDays(autoRemovalMaxDays)).truncatedTo(ChronoUnit.DAYS);
        List<Suggestion> suggestionsToRemove = suggestionManagementService.getSuggestionsUpdatedBeforeNotNew(pointInTime);
        log.info("Removing {} suggestions older than {}.", suggestionsToRemove.size(), pointInTime);
        suggestionsToRemove.forEach(suggestion -> {
            suggestionVoteManagementService.deleteSuggestionVotes(suggestion);
            log.info("Deleting suggestion {} in server {}.",
                    suggestion.getSuggestionId().getId(), suggestion.getSuggestionId().getServerId());
        });
        suggestionManagementService.deleteSuggestion(suggestionsToRemove);
    }

    @Override
    @Transactional
    public CompletableFuture<Void> remindAboutSuggestion(ServerSpecificId suggestionId) {
        Long serverId = suggestionId.getServerId();
        postTargetService.validatePostTarget(SuggestionPostTarget.SUGGESTION_REMINDER, serverId);
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId.getId());
        ServerChannelMessage suggestionServerChannelMessage = ServerChannelMessage
                .builder()
                .serverId(serverId)
                .channelId(suggestion.getChannel().getId())
                .messageId(suggestion.getMessageId())
                .build();
        ServerChannelMessage commandServerChannelMessage = ServerChannelMessage
                .builder()
                .serverId(serverId)
                .channelId(suggestion.getCommandChannel().getId())
                .messageId(suggestion.getCommandMessageId())
                .build();
        SuggestionInfoModel suggestionInfoModel = getSuggestionInfo(suggestionId);
        SuggestionReminderModel model = SuggestionReminderModel
                .builder()
                .serverId(serverId)
                .serverUser(ServerUser.fromAUserInAServer(suggestion.getSuggester()))
                .suggestionCreationDate(suggestion.getCreated())
                .suggestionId(suggestionId.getId())
                .suggestionMessage(suggestionServerChannelMessage)
                .suggestionCommandMessage(commandServerChannelMessage)
                .suggestionInfo(suggestionInfoModel)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(SUGGESTION_REMINDER_TEMPLATE_KEY, model, serverId);
        log.info("Reminding about suggestion {} in server {}.", suggestionId.getId(), serverId);
        List<CompletableFuture<Message>> completableFutures = postTargetService.sendEmbedInPostTarget(messageToSend, SuggestionPostTarget.SUGGESTION_REMINDER, serverId);
        return FutureUtils.toSingleFutureGeneric(completableFutures);
    }

    @Override
    public void cancelSuggestionJobs(Suggestion suggestion) {
        if(suggestion.getSuggestionReminderJobTriggerKey() != null) {
            log.info("Cancelling reminder for suggestion {} in server {}.", suggestion.getSuggestionId().getId(), suggestion.getSuggestionId().getServerId());
            schedulerService.stopTrigger(suggestion.getSuggestionReminderJobTriggerKey());
        }
        if(suggestion.getSuggestionEvaluationJobTriggerKey() != null) {
            log.info("Cancelling evaluation job for suggestion {} in server {}.", suggestion.getSuggestionId().getId(), suggestion.getSuggestionId().getServerId());
            schedulerService.stopTrigger(suggestion.getSuggestionEvaluationJobTriggerKey());
        }
    }

    @Override
    public SuggestionInfoModel getSuggestionInfo(Long serverId, Long suggestionId) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId);
        Long agreements = suggestionVoteManagementService.getDecisionsForSuggestion(suggestion, SuggestionDecision.AGREE);
        Long disagreements = suggestionVoteManagementService.getDecisionsForSuggestion(suggestion, SuggestionDecision.DISAGREE);
        return SuggestionInfoModel
                .builder()
                .agreements(agreements)
                .disagreements(disagreements)
                .build();
    }

    @Override
    public SuggestionInfoModel getSuggestionInfo(ServerSpecificId suggestionId) {
        return getSuggestionInfo(suggestionId.getServerId(), suggestionId.getId());
    }

    @Transactional
    public void deleteSuggestion(Long suggestionId, Long serverId) {
        Suggestion suggestion = suggestionManagementService.getSuggestion(serverId, suggestionId);
        cancelSuggestionJobs(suggestion);
        suggestionVoteManagementService.deleteSuggestionVotes(suggestion);
        suggestionManagementService.deleteSuggestion(suggestion);
    }
}
