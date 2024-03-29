package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.model.BanResult;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.template.command.BanLog;
import dev.sheldan.abstracto.moderation.model.template.command.BanNotificationModel;
import dev.sheldan.abstracto.moderation.model.template.command.UnBanLog;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class BanServiceBean implements BanService {

    public static final String BAN_LOG_TEMPLATE = "ban_log";
    public static final String UN_BAN_LOG_TEMPLATE = "unBan_log";
    public static final String BAN_NOTIFICATION = "ban_notification";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private BanServiceBean self;

    @Autowired
    private MessageService messageService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InfractionService infractionService;

    @Override
    public CompletableFuture<BanResult> banUserWithNotification(ServerUser userToBeBanned, String reason, ServerUser banningUser, Guild guild, Duration deletionDuration) {
        BanLog banLog = BanLog
                .builder()
                .bannedUser(UserDisplay.fromServerUser(userToBeBanned))
                .banningMember(MemberDisplay.fromServerUser(banningUser))
                .deletionDuration(deletionDuration)
                .reason(reason)
                .build();
        BanResult[] result = {BanResult.SUCCESSFUL};
        return sendBanNotification(userToBeBanned, reason, guild)
                .exceptionally(throwable -> {
                    result[0] = BanResult.NOTIFICATION_FAILED;
                    return null;
                })
                .thenCompose(unused -> banUser(guild, userToBeBanned, deletionDuration, reason))
                .thenCompose(unused -> sendBanLogMessage(banLog, guild.getIdLong()))
                .thenAccept(banLogMessage -> self.evaluateAndStoreInfraction(userToBeBanned, guild, reason, banningUser, banLogMessage, deletionDuration))
                .thenApply(unused -> result[0]);
    }

    @Transactional
    public CompletableFuture<Long> evaluateAndStoreInfraction(ServerUser user, Guild guild, String reason, ServerUser banningMember, Message banLogMessage, Duration deletionDuration) {
        if(featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.INFRACTIONS, guild.getIdLong())) {
            Long infractionPoints = configService.getLongValueOrConfigDefault(ModerationFeatureConfig.BAN_INFRACTION_POINTS, guild.getIdLong());
            AUserInAServer bannedUser = userInServerManagementService.loadOrCreateUser(guild.getIdLong(), user.getUserId());
            AUserInAServer banningUser = userInServerManagementService.loadOrCreateUser(banningMember);
            Map<String, String> parameters = new HashMap<>();
            if(deletionDuration == null) {
                deletionDuration = Duration.ZERO;
            }
            parameters.put(INFRACTION_PARAMETER_DELETION_DURATION_KEY, deletionDuration.toString());
            return infractionService.createInfractionWithNotification(bannedUser, infractionPoints, BAN_INFRACTION_TYPE, reason, banningUser, parameters, banLogMessage)
                    .thenApply(Infraction::getId);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> sendBanNotification(ServerUser serverUser, String reason, Guild guild) {
        BanNotificationModel model = BanNotificationModel
                .builder()
                .serverName(guild.getName())
                .reason(reason)
                .build();
        String message = templateService.renderTemplate(BAN_NOTIFICATION, model, guild.getIdLong());
        return messageService.sendMessageToUser(serverUser, message).thenAccept(message1 -> {});
    }

    @Override
    public CompletableFuture<Void> unBanUserWithNotification(Long userId, ServerUser unBanningMember, Guild guild) {
        UnBanLog banLog = UnBanLog
                .builder()
                .bannedUser(UserDisplay.fromId(userId))
                .unBanningMember(MemberDisplay.fromServerUser(unBanningMember))
                .build();
        return unbanUser(guild, userId)
                .thenCompose(unused -> self.sendUnBanLogMessage(banLog, guild.getIdLong(), UN_BAN_LOG_TEMPLATE));
    }

    @Override
    public CompletableFuture<Void> banUser(Guild guild, ServerUser userToBeBanned, Duration deletionDuration, String reason) {
        log.info("Banning user {} in guild {}.", userToBeBanned.getUserId(), guild.getId());
        if(deletionDuration == null || deletionDuration.isNegative()) {
            deletionDuration = Duration.ZERO;
        }
        return guild.ban(UserSnowflake.fromId(userToBeBanned.getUserId()), (int) deletionDuration.getSeconds(), TimeUnit.SECONDS).reason(reason).submit();
    }

    @Override
    public CompletableFuture<Void> unbanUser(Guild guild, Long userId) {
        log.info("Unbanning user {} in guild {}.", userId, guild.getId());
        return guild.unban(UserSnowflake.fromId(userId)).submit();
    }

    @Override
    public CompletableFuture<Void> softBanUser(Guild guild, ServerUser user, Duration delDays) {
        return banUser(guild, user, delDays, "")
                .thenCompose(unused -> unbanUser(guild, user.getUserId()));
    }

    public CompletableFuture<Message> sendBanLogMessage(BanLog banLog, Long guildId) {
        MessageToSend banLogMessage = renderBanMessage(banLog, guildId);
        log.debug("Sending ban log message in guild {}.", guildId);
        List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(banLogMessage, ModerationPostTarget.BAN_LOG, guildId);
        return FutureUtils.toSingleFutureGeneric(messageFutures).thenApply(unused -> messageFutures.get(0).join());
    }

    public MessageToSend renderBanMessage(BanLog banLog, Long guildId) {
        return templateService.renderEmbedTemplate(BAN_LOG_TEMPLATE, banLog, guildId);
    }

    public CompletableFuture<Void> sendUnBanLogMessage(UnBanLog banLog, Long guildId, String template) {
        MessageToSend banLogMessage = templateService.renderEmbedTemplate(template, banLog, guildId);
        log.debug("Sending unban log message in guild {}.", guildId);
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(banLogMessage, ModerationPostTarget.UN_BAN_LOG, guildId));
    }
}
