package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.model.BanResult;
import dev.sheldan.abstracto.moderation.model.template.command.BanLog;
import dev.sheldan.abstracto.moderation.model.template.command.BanNotificationModel;
import dev.sheldan.abstracto.moderation.model.template.command.UnBanLog;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

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

    @Override
    public CompletableFuture<BanResult> banUserWithNotification(User user, String reason, Member banningMember, Integer deletionDays) {
        BanLog banLog = BanLog
                .builder()
                .bannedUser(user)
                .banningMember(banningMember)
                .deletionDays(deletionDays)
                .reason(reason)
                .build();
        Guild guild = banningMember.getGuild();
        CompletableFuture<BanResult> returningFuture = new CompletableFuture<>();
        sendBanNotification(user, reason, guild)
                .whenComplete((unused, throwable) -> banUser(guild, user, deletionDays, reason)
                .thenCompose(unused1 -> sendBanLogMessage(banLog, guild.getIdLong()))
                .thenAccept(unused1 -> {
                    if(throwable != null)  {
                        returningFuture.complete(BanResult.NOTIFICATION_FAILED);
                    } else {
                        returningFuture.complete(BanResult.SUCCESSFUL);
                    }
                }));
        return returningFuture;
    }

    private CompletableFuture<Void> sendBanNotification(User user, String reason, Guild guild) {
        BanNotificationModel model = BanNotificationModel
                .builder()
                .serverName(guild.getName())
                .reason(reason)
                .build();
        String message = templateService.renderTemplate(BAN_NOTIFICATION, model, guild.getIdLong());
        return messageService.sendMessageToUser(user, message).thenAccept(message1 -> {});
    }

    @Override
    public CompletableFuture<Void> unBanUserWithNotification(User user, Member unBanningMember) {
        Guild guild = unBanningMember.getGuild();
        UnBanLog banLog = UnBanLog
                .builder()
                .bannedUser(user)
                .unBanningMember(unBanningMember)
                .build();
        return unbanUser(guild, user)
                .thenCompose(unused -> self.sendUnBanLogMessage(banLog, guild.getIdLong(), UN_BAN_LOG_TEMPLATE));
    }

    @Override
    public CompletableFuture<Void> banUser(Guild guild, User user, Integer deletionDays, String reason) {
        log.info("Banning user {} in guild {}.", user.getIdLong(), guild.getId());
        return guild.ban(user, deletionDays, reason).submit();
    }

    @Override
    public CompletableFuture<Void> unbanUser(Guild guild, User user) {
        log.info("Unbanning user {} in guild {}.", user.getIdLong(), guild.getId());
        return guild.unban(user).submit();
    }

    @Override
    public CompletableFuture<Void> softBanUser(Guild guild, User user, Duration delDays) {
        Long days = delDays.toDays();
        return banUser(guild, user, days.intValue(), "")
                .thenCompose(unused -> unbanUser(guild, user));
    }

    public CompletableFuture<Void> sendBanLogMessage(BanLog banLog, Long guildId) {
        MessageToSend banLogMessage = templateService.renderEmbedTemplate(BAN_LOG_TEMPLATE, banLog, guildId);
        log.debug("Sending ban log message in guild {}.", guildId);
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(banLogMessage, ModerationPostTarget.BAN_LOG, guildId));
    }

    public CompletableFuture<Void> sendUnBanLogMessage(UnBanLog banLog, Long guildId, String template) {
        MessageToSend banLogMessage = templateService.renderEmbedTemplate(template, banLog, guildId);
        log.debug("Sending unban log message in guild {}.", guildId);
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(banLogMessage, ModerationPostTarget.UN_BAN_LOG, guildId));
    }
}
