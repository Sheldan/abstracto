package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.BanResult;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.template.command.BanNotificationModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

    @Override
    public CompletableFuture<BanResult> banUserWithNotification(ServerUser userToBeBanned, String reason, ServerUser banningUser, Guild guild, Duration deletionDuration) {
        BanResult[] result = {BanResult.SUCCESSFUL};
        return sendBanNotification(userToBeBanned, reason, guild)
                .exceptionally(throwable -> {
                    result[0] = BanResult.NOTIFICATION_FAILED;
                    return null;
                })
                .thenCompose(unused -> banUser(guild, userToBeBanned, deletionDuration, reason))
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
    public CompletableFuture<Void> unbanUser(Guild guild, Long userId) {
        log.info("Unbanning user {} in guild {}.", userId, guild.getId());
        return guild.unban(UserSnowflake.fromId(userId)).submit();
    }

    @Override
    public CompletableFuture<Void> softBanUser(Guild guild, ServerUser user, Duration delDays) {
        return banUser(guild, user, delDays, "")
                .thenCompose(unused -> unbanUser(guild, user.getUserId()));
    }

}
