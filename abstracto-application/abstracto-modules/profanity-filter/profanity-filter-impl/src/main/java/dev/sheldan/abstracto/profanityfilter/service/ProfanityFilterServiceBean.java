package dev.sheldan.abstracto.profanityfilter.service;

import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.database.ProfanityRegex;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ProfanityRegexManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.profanityfilter.config.ProfanityFilterFeatureDefinition;
import dev.sheldan.abstracto.profanityfilter.config.ProfanityFilterMode;
import dev.sheldan.abstracto.profanityfilter.config.ProfanityFilterPostTarget;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUse;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUserInAServer;
import dev.sheldan.abstracto.profanityfilter.model.template.ProfanityReportModel;
import dev.sheldan.abstracto.profanityfilter.service.management.ProfanityUseManagementService;
import dev.sheldan.abstracto.profanityfilter.service.management.ProfanityUserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProfanityFilterServiceBean implements ProfanityFilterService {

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private ProfanityUserInServerManagementService profanityUserInServerManagementService;

    @Autowired
    private ProfanityUseManagementService profanityUseManagementService;

    @Autowired
    private ProfanityRegexManagementService profanityRegexManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MetricService metricService;

    @Autowired
    private RoleImmunityService roleImmunityService;

    @Autowired
    private ProfanityFilterServiceBean self;

    private static final String PROFANITY_REPORT_TEMPLATE_KEY = "profanityDetection_listener_report";

    public static final String MODERATION_PURGE_METRIC = "profanity.filter";
    public static final String STEP = "step";

    private static final CounterMetric PROFANITIES_DETECTED_METRIC =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(STEP, "detection")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();


    private static final CounterMetric PROFANITIES_AGREEMENT =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(STEP, "agreement")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    private static final CounterMetric PROFANITIES_DISAGREEMENT =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(STEP, "disagreement")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    @Override
    public CompletableFuture<Void> createProfanityReport(Message message, ProfanityRegex foundProfanityRegex) {
        ProfanityReportModel reportModel = ProfanityReportModel
                .builder()
                .profaneMessage(message)
                .profanityGroupKey(foundProfanityRegex.getGroup().getGroupName())
                .profanityRegexName(foundProfanityRegex.getRegexName())
                .build();
        Long serverId = message.getGuild().getIdLong();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(PROFANITY_REPORT_TEMPLATE_KEY, reportModel, serverId);
        List<CompletableFuture<Message>> messageFutures = postTargetService
                .sendEmbedInPostTarget(messageToSend, ProfanityFilterPostTarget.PROFANITY_FILTER_QUEUE, serverId);
        Long profanityRegexId = foundProfanityRegex.getId();
        return FutureUtils.toSingleFutureGeneric(messageFutures).thenCompose(aVoid -> {
            Message createdMessage = messageFutures.get(0).join();
            return self.afterReportCreation(message, serverId, profanityRegexId, createdMessage);
        });
    }

    @Transactional
    public CompletableFuture<Void> afterReportCreation(Message message, Long serverId, Long profanityRegexId, Message createdMessage) {
        if(featureModeService.featureModeActive(ProfanityFilterFeatureDefinition.PROFANITY_FILTER, serverId, ProfanityFilterMode.PROFANITY_VOTE)) {
            CompletableFuture<Void> firstReaction = reactionService.addReactionToMessageAsync(ProfanityFilterService.REPORT_AGREE_EMOTE, serverId, createdMessage);
            CompletableFuture<Void> secondReaction = reactionService.addReactionToMessageAsync(ProfanityFilterService.REPORT_DISAGREE_EMOTE, serverId, createdMessage);
            return CompletableFuture.allOf(firstReaction, secondReaction).thenAccept(aVoid1 -> {
                log.debug("Reaction added to message {} for a profanity report.", message.getId());
                self.persistProfanityReport(message, createdMessage, profanityRegexId);
            });
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public boolean isMessageProfanityReport(Long messageId) {
        return profanityUseManagementService.getProfanityUseViaReportMessageId(messageId).isPresent();
    }

    @Override
    public void verifyProfanityUse(ProfanityUse profanityUse, VoteResult result) {
        switch(result) {
            case DISAGREEMENT: 
                profanityUse.setConfirmed(false); 
                metricService.incrementCounter(PROFANITIES_DISAGREEMENT); 
                break;
            case AGREEMENT: 
                profanityUse.setConfirmed(true); 
                metricService.incrementCounter(PROFANITIES_AGREEMENT);
                deleteProfaneMessage(profanityUse);
                break;
            default: throw new IllegalArgumentException("Final vote result given. No mapping to action found.");
        }
        profanityUse.setVerified(true);
    }

    @Override
    public Long getPositiveReportCountForUser(AUserInAServer aUserInAServer) {
        ProfanityUserInAServer profanityUserInAServer = profanityUserInServerManagementService.getProfanityUser(aUserInAServer);
        return getPositiveReportCountForUser(profanityUserInAServer);
    }

    @Override
    public Long getPositiveReportCountForUser(ProfanityUserInAServer aUserInAServer) {
        return profanityUseManagementService.getPositiveReports(aUserInAServer);
    }

    @Override
    public Long getFalseProfanityReportCountForUser(AUserInAServer aUserInAServer) {
        ProfanityUserInAServer profanityUserInAServer = profanityUserInServerManagementService.getProfanityUser(aUserInAServer);
        return getFalseProfanityReportCountForUser(profanityUserInAServer);
    }

    @Override
    public Long getFalseProfanityReportCountForUser(ProfanityUserInAServer aUserInAServer) {
        return profanityUseManagementService.getFalsePositiveReports(aUserInAServer);
    }

    @Override
    public List<ServerChannelMessage> getRecentPositiveReports(AUserInAServer aUserInAServer, int count) {
        ProfanityUserInAServer profanityUserInAServer = profanityUserInServerManagementService.getProfanityUser(aUserInAServer);
        return getRecentPositiveReports(profanityUserInAServer, count);
    }

    private void deleteProfaneMessage(ProfanityUse profanityUse) {
        messageService.deleteMessageInChannelInServer(profanityUse.getServer().getId(), profanityUse.getProfaneChannel().getId(), profanityUse.getProfaneMessageId())
                .exceptionally(throwable -> {
                    log.info("Failed to delete profane message ");
                   return null;
                });
    }

    @Override
    public List<ServerChannelMessage> getRecentPositiveReports(ProfanityUserInAServer aUserInAServer, int count) {
        return profanityUseManagementService.getMostRecentProfanityReports(aUserInAServer, count)
                .stream()
                .map(profanityUse -> ServerChannelMessage
                        .builder()
                        .messageId(profanityUse.getReportMessageId())
                        .channelId(profanityUse.getReportChannel().getId())
                        .serverId(profanityUse.getServer().getId()).build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void persistProfanityReport(Message profaneMessage, Message reportMessage, Long profanityRegexId) {
        ServerChannelMessage profaneMessageObj = ServerChannelMessage.fromMessage(profaneMessage);
        ServerChannelMessage reportMessageObj = ServerChannelMessage.fromMessage(reportMessage);
        ProfanityRegex profanityRegex = profanityRegexManagementService.getProfanityRegexViaId(profanityRegexId);
        AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(profaneMessage.getMember());
        ProfanityUserInAServer profaneUser = profanityUserInServerManagementService.getOrCreateProfanityUser(aUserInAServer);
        profanityUseManagementService.createProfanityUse(profaneMessageObj, reportMessageObj, profaneUser, profanityRegex.getGroup());
    }

    @Override
    public boolean isImmuneAgainstProfanityFilter(Member member) {
        return roleImmunityService.isImmune(member, PROFANITY_FILTER_EFFECT_KEY);
    }

    public void handleProfaneMessage(Message message, ProfanityRegex foundProfanityGroup) {
        metricService.incrementCounter(PROFANITIES_DETECTED_METRIC);
        if(featureModeService.featureModeActive(ProfanityFilterFeatureDefinition.PROFANITY_FILTER, message.getGuild().getIdLong(), ProfanityFilterMode.PROFANITY_REPORT)) {
            createProfanityReport(message, foundProfanityGroup).exceptionally(throwable -> {
                log.error("Failed to report or persist profanities in server {} for message {} in channel {}.",
                        message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong(), throwable);
                return null;
            });
        }
        if(featureModeService.featureModeActive(ProfanityFilterFeatureDefinition.PROFANITY_FILTER, message.getGuild().getIdLong(), ProfanityFilterMode.AUTO_DELETE_PROFANITIES)) {
            messageService.deleteMessage(message).exceptionally(throwable -> {
                log.error("Failed to delete profanity message with id {} in channel {} in server {}.",
                        message.getIdLong(), message.getChannel().getIdLong(), message.getGuild().getIdLong(), throwable);
                return null;
            });
        }
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(PROFANITIES_AGREEMENT, "Amount of profanity votes resulting in agreement");
        metricService.registerCounter(PROFANITIES_DISAGREEMENT, "Amount of profanity votes resulting in disagreement");
        metricService.registerCounter(PROFANITIES_DETECTED_METRIC, "Amount of profanities detected");
    }
}
