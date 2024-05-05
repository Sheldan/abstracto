package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureMap;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.moderation.model.BanResult;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.template.command.BanNotificationModel;
import dev.sheldan.abstracto.moderation.model.template.listener.UserBannedLogModel;
import dev.sheldan.abstracto.moderation.model.template.listener.UserUnBannedLogModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class BanServiceBean implements BanService {

    public static final String BAN_NOTIFICATION = "ban_notification";

    @Autowired
    private TemplateService templateService;

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

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private UserService userService;

    public static final String USER_BANNED_NOTIFICATION_TEMPLATE = "userBanned_listener_notification";
    private static final String USER_UN_BANNED_NOTIFICATION_TEMPLATE = "userUnBanned_listener_notification";

    @Override
    public CompletableFuture<BanResult> banUserWithNotification(ServerUser userToBeBanned, String reason, ServerUser banningUser, Guild guild, Duration deletionDuration) {
        BanResult[] result = {BanResult.SUCCESSFUL};
        return sendBanNotification(userToBeBanned, reason, guild)
                .exceptionally(throwable -> {
                    result[0] = BanResult.NOTIFICATION_FAILED;
                    return null;
                })
                .thenCompose(unused -> banUser(guild, userToBeBanned, deletionDuration, reason))
                .thenCompose(unused -> self.composeAndSendBanLogMessage(userToBeBanned, banningUser, reason))
                .thenAccept(banLogMessage -> self.evaluateAndStoreInfraction(userToBeBanned, guild, reason, banningUser, deletionDuration))
                .thenApply(unused -> result[0]);
    }

    @Transactional
    public CompletableFuture<Long> evaluateAndStoreInfraction(ServerUser user, Guild guild, String reason, ServerUser banningMember, Duration deletionDuration) {
        if(featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.INFRACTIONS, guild.getIdLong())) {
            Long infractionPoints = configService.getLongValueOrConfigDefault(ModerationFeatureConfig.BAN_INFRACTION_POINTS, guild.getIdLong());
            AUserInAServer bannedUser = userInServerManagementService.loadOrCreateUser(guild.getIdLong(), user.getUserId());
            AUserInAServer banningUser = userInServerManagementService.loadOrCreateUser(banningMember);
            Map<String, String> parameters = new HashMap<>();
            if(deletionDuration == null) {
                deletionDuration = Duration.ZERO;
            }
            parameters.put(INFRACTION_PARAMETER_DELETION_DURATION_KEY, deletionDuration.toString());
            return infractionService.createInfractionWithNotification(bannedUser, infractionPoints, BAN_INFRACTION_TYPE, reason, banningUser, parameters)
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
    public CompletableFuture<Void> banUser(Guild guild, ServerUser userToBeBanned, Duration deletionDuration, String reason) {
        log.info("Banning user {} in guild {}.", userToBeBanned.getUserId(), guild.getId());
        if(deletionDuration == null || deletionDuration.isNegative()) {
            deletionDuration = Duration.ZERO;
        }
        return guild.ban(UserSnowflake.fromId(userToBeBanned.getUserId()), (int) deletionDuration.getSeconds(), TimeUnit.SECONDS).reason(reason).submit();
    }

    @Override
    public CompletableFuture<Void> unbanUser(Guild guild, User user, Member memberPerforming) {
        log.info("Unbanning user {} in guild {}.", user.getIdLong(), guild.getId());
        return guild.unban(user).submit().thenCompose(unused -> self.composeAndSendUnBanLogMessage(guild, user, memberPerforming));
    }

    @Transactional
    public CompletionStage<Void> composeAndSendUnBanLogMessage(Guild guild, User user, Member memberPerforming) {
        UserUnBannedLogModel model = UserUnBannedLogModel
                .builder()
                .unBannedUser(UserDisplay.fromUser(user))
                .unBanningUser(UserDisplay.fromUser(memberPerforming.getUser()))
                .reason(null)
                .build();
        return sendUnBanLogMessage(model, guild.getIdLong());
    }

    @Transactional
    public CompletableFuture<Void> composeAndSendBanLogMessage(ServerUser serverUserToBeBanned, ServerUser serverUserBanning, String reason) {
        CompletableFutureMap<Long, User> userMap = userService.retrieveUsersMapped(Arrays.asList(serverUserToBeBanned.getUserId(), serverUserBanning.getUserId()));
        return userMap.getMainFuture().thenCompose(unused -> {
            User userToBeBanned = userMap.getElement(serverUserToBeBanned.getUserId());
            User banningUser = userMap.getElement(serverUserBanning.getUserId());
            UserBannedLogModel model = UserBannedLogModel
                    .builder()
                    .bannedUser(UserDisplay.fromUser(userToBeBanned))
                    .banningUser(UserDisplay.fromUser(banningUser))
                    .reason(reason)
                    .build();
            return self.sendBanLogMessage(model, serverUserToBeBanned.getServerId());
        }).exceptionally(throwable -> {
           log.warn("Failed to load users ({}, {}) for ban log message.", serverUserToBeBanned.getUserId(), serverUserBanning.getUserId(), throwable);
            UserBannedLogModel model = UserBannedLogModel
                    .builder()
                    .bannedUser(UserDisplay.fromId(serverUserToBeBanned.getUserId()))
                    .banningUser(UserDisplay.fromId(serverUserBanning.getUserId()))
                    .reason(reason)
                    .build();
            self.sendBanLogMessage(model, serverUserToBeBanned.getServerId());
            return null;
        });
    }

    @Override
    @Transactional
    public CompletableFuture<Void> sendUnBanLogMessage(UserUnBannedLogModel model, Long serverId) {
        MessageToSend messageToSend = templateService.renderEmbedTemplate(USER_UN_BANNED_NOTIFICATION_TEMPLATE, model, serverId);
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, ModerationPostTarget.BAN_LOG, serverId));
    }

    @Override
    @Transactional
    public CompletableFuture<Void> sendBanLogMessage(UserBannedLogModel model, Long serverId) {
        MessageToSend messageToSend = templateService.renderEmbedTemplate(USER_BANNED_NOTIFICATION_TEMPLATE, model, serverId);
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, ModerationPostTarget.BAN_LOG, serverId));
    }

}
