package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.CompletableFutureMap;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureConfig;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.template.command.KickLogModel;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class KickServiceBean implements KickService {

    public static final String KICK_LOG_TEMPLATE = "kick_log";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private InfractionService infractionService;

    @Autowired
    private UserService userService;

    @Autowired
    private KickServiceBean self;

    @Override
    public CompletableFuture<Void> kickMember(Member kickedMember, Member kickingMember, String reason)  {
        Guild guild = kickedMember.getGuild();
        log.info("Kicking user {} from guild {}", kickedMember.getUser().getIdLong(), guild.getIdLong());
        CompletableFuture<Void> kickFuture = guild.kick(kickedMember, reason).submit();
        CompletableFuture<Message> logFuture = sendKickLog(kickedMember.getUser(), ServerUser.fromMember(kickedMember), kickingMember.getUser(), ServerUser.fromMember(kickingMember), reason, guild.getIdLong());
        return CompletableFuture.allOf(kickFuture, logFuture)
                .thenAccept(unused -> self.storeInfraction(kickedMember, kickingMember, reason, logFuture.join(), guild.getIdLong()));
    }

    @Override
    public CompletableFuture<Void> kickMember(Guild guild, ServerUser kickedUser, String reason, ServerUser kickingUser) {
        CompletableFuture<Void> kickFuture = guild.kick(UserSnowflake.fromId(kickedUser.getUserId())).submit();
        CompletableFuture<Message> logFuture = sendKickLog(kickedUser, kickingUser, reason, guild.getIdLong());
        return CompletableFuture.allOf(kickFuture, logFuture)
                .thenAccept(unused -> self.storeInfraction(kickedUser, kickingUser, reason, logFuture.join(), guild.getIdLong()));
    }

    @Transactional
    public CompletableFuture<Long> storeInfraction(Member member, Member kickingMember, String reason, Message logMessage, Long serverId) {
        if(featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.INFRACTIONS, serverId)) {
            Long infractionPoints = configService.getLongValueOrConfigDefault(ModerationFeatureConfig.KICK_INFRACTION_POINTS, serverId);
            AUserInAServer kickedUser = userInServerManagementService.loadOrCreateUser(member);
            AUserInAServer kickingUser = userInServerManagementService.loadOrCreateUser(kickingMember);
            return infractionService.createInfractionWithNotification(kickedUser, infractionPoints, KICK_INFRACTION_TYPE, reason, kickingUser, logMessage).thenApply(Infraction::getId);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    @Transactional
    public CompletableFuture<Long> storeInfraction(ServerUser member, ServerUser kickingMember, String reason, Message logMessage, Long serverId) {
        if(featureFlagService.getFeatureFlagValue(ModerationFeatureDefinition.INFRACTIONS, serverId)) {
            Long infractionPoints = configService.getLongValueOrConfigDefault(ModerationFeatureConfig.KICK_INFRACTION_POINTS, serverId);
            AUserInAServer kickedUser = userInServerManagementService.loadOrCreateUser(member);
            AUserInAServer kickingUser = userInServerManagementService.loadOrCreateUser(kickingMember);
            return infractionService.createInfractionWithNotification(kickedUser, infractionPoints, KICK_INFRACTION_TYPE, reason, kickingUser, logMessage).thenApply(Infraction::getId);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    public CompletableFuture<Message> sendKickLog(User kickedUser, ServerUser kickedServerUser, User kickingUser, ServerUser kickingServerUser, String reason, Long serverId)  {
        KickLogModel kickLogModel = KickLogModel
                .builder()
                .kickedUser(kickedUser != null ? UserDisplay.fromUser(kickedUser) : UserDisplay.fromServerUser(kickedServerUser))
                .kickingUser(kickingUser != null ? UserDisplay.fromUser(kickingUser) : UserDisplay.fromServerUser(kickingServerUser))
                .reason(reason)
                .build();
        return sendKicklog(kickLogModel, serverId);
    }

    public CompletableFuture<Message> sendKickLog(ServerUser kickedMember, ServerUser kickingMember, String reason, Long serverId)  {
        CompletableFutureMap<Long, User> userMap = userService.retrieveUsersMapped(Arrays.asList(kickedMember.getUserId(), kickingMember.getUserId()));
        return userMap.getMainFuture().thenCompose(unused -> {
            User kickedUser = userMap.getElement(kickedMember.getUserId());
            User kickingUser = userMap.getElement(kickingMember.getUserId());
            return self.sendKickLog(kickedUser, kickedMember, kickingUser, kickingMember, reason, serverId);
        }).exceptionally(throwable -> {
            log.warn("Failed to fetch users ({}, {}) for kick event logging in server {}.", kickingMember.getUserId(), kickedMember.getUserId(), serverId, throwable);
            self.sendKickLog(null, kickedMember, null, kickingMember, reason, serverId);
            return null;
        });
    }

    public CompletableFuture<Message> sendKicklog(KickLogModel kickLogModel, Long serverId) {
        MessageToSend warnLogMessage = templateService.renderEmbedTemplate(KICK_LOG_TEMPLATE, kickLogModel, serverId);
        log.debug("Sending kick log message in guild {}.", serverId);
        List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(warnLogMessage, ModerationPostTarget.KICK_LOG, serverId);
        return FutureUtils.toSingleFutureGeneric(messageFutures).thenApply(unused -> messageFutures.get(0).join());
    }

}
