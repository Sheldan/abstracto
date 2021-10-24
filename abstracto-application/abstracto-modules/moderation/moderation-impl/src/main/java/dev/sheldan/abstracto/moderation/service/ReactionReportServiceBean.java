package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.posttarget.ReactionReportPostTarget;
import dev.sheldan.abstracto.moderation.model.database.ModerationUser;
import dev.sheldan.abstracto.moderation.model.database.ReactionReport;
import dev.sheldan.abstracto.moderation.model.template.listener.ReportReactionNotificationModel;
import dev.sheldan.abstracto.moderation.service.management.ModerationUserManagementService;
import dev.sheldan.abstracto.moderation.service.management.ReactionReportManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
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

    private static final String REACTION_REPORT_TEMPLATE_KEY = "reactionReport_notification";

    @Override
    public CompletableFuture<Void> createReactionReport(CachedMessage reportedMessage, ServerUser reporter) {
        AUserInAServer reportedUser = userInServerManagementService.loadOrCreateUser(reportedMessage.getAuthorAsServerUser());
        AUserInAServer reportingUser = userInServerManagementService.loadOrCreateUser(reporter);
        Optional<ModerationUser> moderationUserOptional = moderationUserManagementService.findModerationUser(reportingUser);
        Long serverId = reporter.getServerId();
        Long cooldownSeconds = configService.getLongValueOrConfigDefault(REACTION_REPORT_COOLDOWN, serverId);
        Duration maxAge = Duration.of(cooldownSeconds, ChronoUnit.SECONDS);
        if(moderationUserOptional.isPresent()) {
            ModerationUser reporterModerationUser = moderationUserOptional.get();
            Instant minAllowedReportTime = Instant.now().minus(maxAge);
            if(reporterModerationUser.getLastReportTimeStamp() != null && reporterModerationUser.getLastReportTimeStamp().isAfter(minAllowedReportTime)) {
                log.info("User {} in server {} reported user {} within the cooldown. Ignoring.", reporter.getUserId(), serverId, reportedMessage.getAuthor().getAuthorId());
                return CompletableFuture.completedFuture(null);
            }
        }
        log.info("User {} in server {} reported user {}..", reporter.getUserId(), serverId, reportedMessage.getAuthor().getAuthorId());
        Optional<ReactionReport> recentReportOptional = reactionReportManagementService.findRecentReactionReportAboutUser(reportedUser, maxAge);
        if(!recentReportOptional.isPresent()) {
            ReportReactionNotificationModel model = ReportReactionNotificationModel
                    .builder()
                    .reportCount(1)
                    .reportedMessage(reportedMessage)
                    .build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate(REACTION_REPORT_TEMPLATE_KEY, model, serverId);
            List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, ReactionReportPostTarget.REACTION_REPORTS, serverId);
            return FutureUtils.toSingleFutureGeneric(messageFutures)
                    .thenAccept(unused -> self.createReactionReportInDb(reportedMessage, messageFutures.get(0).join(), reporter));
        } else {
            ReactionReport report = recentReportOptional.get();
            log.info("Report is already present in channel {} with message {}. Updating field.", report.getReportChannel().getId(), report.getReportMessageId());
            report.setReportCount(report.getReportCount() + 1);
            TextChannel reportTextChannel = channelService.getTextChannelFromServer(serverId, report.getReportChannel().getId());
            return channelService.editFieldValueInMessage(reportTextChannel, report.getReportMessageId(), 0, report.getReportCount().toString())
                    .thenAccept(message -> self.updateModerationUserReportCooldown(reporter));
        }
    }

    @Transactional
    public void createReactionReportInDb(CachedMessage cachedMessage, Message reportMessage, ServerUser reporter) {
        if(reportMessage == null) {
            log.info("Creation reaction report about message {} was not sent - post target might be disabled in server {}.", cachedMessage.getMessageId(), cachedMessage.getServerId());
        } else {
            log.info("Creation reaction report in message {} about message {} in database.", reportMessage.getIdLong(), cachedMessage.getMessageId());
            reactionReportManagementService.createReactionReport(cachedMessage, reportMessage);
            updateModerationUserReportCooldown(reporter);
        }
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
}
