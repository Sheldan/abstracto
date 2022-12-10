package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.feature.mode.ReportReactionMode;
import dev.sheldan.abstracto.moderation.config.posttarget.ReactionReportPostTarget;
import dev.sheldan.abstracto.moderation.listener.manager.ReportMessageCreatedListenerManager;
import dev.sheldan.abstracto.moderation.model.database.ModerationUser;
import dev.sheldan.abstracto.moderation.model.database.ReactionReport;
import dev.sheldan.abstracto.moderation.model.template.listener.ReportReactionNotificationModel;
import dev.sheldan.abstracto.moderation.service.management.ModerationUserManagementService;
import dev.sheldan.abstracto.moderation.service.management.ReactionReportManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ReactionReportServiceBean implements ReactionReportService {

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ReactionReportManagementService reactionReportManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ModerationUserManagementService moderationUserManagementService;

    @Autowired
    private ReactionReportServiceBean self;

    @Autowired
    private CacheEntityService cacheEntityService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ReportMessageCreatedListenerManager reportMessageCreatedListenerManager;

    private static final String REACTION_REPORT_TEMPLATE_KEY = "reactionReport_notification";
    public static final String REACTION_REPORT_MODAL_ORIGIN = "reportMessageModal";
    public static final String REACTION_REPORT_RESPONSE_TEMPLATE = "reactionReport_response";
    public static final String REACTION_REPORT_FAILURE_RESPONSE_TEMPLATE = "reactionReport_failure_response";
    public static final String REACTION_REPORT_COOLDOWN_RESPONSE_TEMPLATE = "reactionReport_cooldown_response";
    public static final String REACTION_REPORT_OWN_MESSAGE_RESPONSE_TEMPLATE = "reactionReport_own_message_response";

    @Override
    public CompletableFuture<Void> createReactionReport(CachedMessage reportedMessage, ServerUser reporter, String context) {
        AUserInAServer reportedUser = userInServerManagementService.loadOrCreateUser(reportedMessage.getAuthorAsServerUser());
        Long serverId = reporter.getServerId();
        log.info("User {} in server {} was reported on message {}", reportedMessage.getAuthor().getAuthorId(), serverId, reportedMessage.getMessageId());
        Long cooldownSeconds = configService.getLongValueOrConfigDefault(REACTION_REPORT_COOLDOWN, serverId);
        Duration maxAge = Duration.of(cooldownSeconds, ChronoUnit.SECONDS);
        Optional<ReactionReport> recentReportOptional = reactionReportManagementService.findRecentReactionReportAboutUser(reportedUser, maxAge);
        boolean singularMessage = featureModeService.featureModeActive(ModerationFeatureDefinition.REPORT_REACTIONS, serverId, ReportReactionMode.SINGULAR_MESSAGE);
        boolean anonymous = featureModeService.featureModeActive(ModerationFeatureDefinition.REPORT_REACTIONS, reporter.getServerId(), ReportReactionMode.ANONYMOUS);
        if(recentReportOptional.isPresent() && singularMessage) {
            ReactionReport report = recentReportOptional.get();
            log.info("Report is already present in channel {} with message {}. Updating field.", report.getReportChannel().getId(), report.getReportMessageId());
            report.setReportCount(report.getReportCount() + 1);
            GuildMessageChannel reportTextChannel = channelService.getMessageChannelFromServer(serverId, report.getReportChannel().getId());
            return channelService.editFieldValueInMessage(reportTextChannel, report.getReportMessageId(), 0, report.getReportCount().toString())
                    .thenAccept(message -> self.updateModerationUserReportCooldown(reporter));
        } else {
            ReportReactionNotificationModel model = ReportReactionNotificationModel
                    .builder()
                    .reportCount(1)
                    .context(context)
                    .singularMessage(singularMessage)
                    .reportedMessage(reportedMessage)
                    .build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate(REACTION_REPORT_TEMPLATE_KEY, model, serverId);
            List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, ReactionReportPostTarget.REACTION_REPORTS, serverId);
            return FutureUtils.toSingleFutureGeneric(messageFutures)
                    .thenAccept(unused -> reportMessageCreatedListenerManager.sendReportMessageCreatedEvent(reportedMessage, messageFutures.get(0).join(), anonymous ? null : reporter))
                    .thenAccept(unused -> {
                        if(!anonymous) {
                            self.createReactionReportInDb(reportedMessage, messageFutures.get(0).join(), reporter);
                        }
                    });
        }
    }

    @Override
    public CompletableFuture<Void> createReactionReport(Message message, ServerUser reporter, String context) {
        return cacheEntityService.buildCachedMessageFromMessage(message)
                .thenCompose(cachedMessage -> createReactionReport(cachedMessage, reporter, context));
    }

    @Transactional
    public void createReactionReportInDb(CachedMessage cachedMessage, Message reportMessage, ServerUser reporter) {
        if(reportMessage == null) {
            log.info("Creation reaction report about message {} was not sent - post target might be disabled in server {}.", cachedMessage.getMessageId(), cachedMessage.getServerId());
        } else {
            log.info("Creation reaction report in message {} about message {} in database.", reportMessage.getIdLong(), cachedMessage.getMessageId());
            reactionReportManagementService.createReactionReport(cachedMessage, reportMessage);
        }
        updateModerationUserReportCooldown(reporter);
    }

    @Transactional
    public void updateModerationUserReportCooldown(ServerUser reporter) {
        AUserInAServer reporterAUserInServer = userInServerManagementService.loadOrCreateUser(reporter);
        Optional<ModerationUser> optionalModerationUser = moderationUserManagementService.findModerationUser(reporterAUserInServer);
        Instant reportTimeStamp = Instant.now();
        if(optionalModerationUser.isPresent()) {
            log.info("Updating last report time of user {}.", reporter.getUserId());
            optionalModerationUser.get().setLastReportTimeStamp(reportTimeStamp);
        } else {
            log.info("Creating new moderation user instance for user {} to track report cooldowns.", reporter.getUserId());
            moderationUserManagementService.createModerationUserWithReportTimeStamp(reporterAUserInServer, reportTimeStamp);
        }
    }

    @Override
    public boolean allowedToReport(ServerUser reporter) {
        Long cooldownSeconds = configService.getLongValueOrConfigDefault(REACTION_REPORT_COOLDOWN, reporter.getServerId());
        Duration maxAge = Duration.of(cooldownSeconds, ChronoUnit.SECONDS);
        AUserInAServer reportingUser = userInServerManagementService.loadOrCreateUser(reporter);
        Optional<ModerationUser> moderationUserOptional = moderationUserManagementService.findModerationUser(reportingUser);
        if(moderationUserOptional.isPresent()) {
            ModerationUser reporterModerationUser = moderationUserOptional.get();
            Instant minAllowedReportTime = Instant
                    .now()
                    .minus(maxAge);
            if(reporterModerationUser.getLastReportTimeStamp() != null) {
                return !reporterModerationUser.getLastReportTimeStamp().isAfter(minAllowedReportTime);
            }
        }
        return true;
    }
}
