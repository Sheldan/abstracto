package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.model.template.command.BanLog;
import dev.sheldan.abstracto.moderation.model.template.command.UnBanLog;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class BanServiceBean implements BanService {

    public static final String BAN_LOG_TEMPLATE = "ban_log";
    public static final String UN_BAN_LOG_TEMPLATE = "unBan_log";

    @Autowired
    private GuildService guildService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private BanServiceBean self;

    @Override
    public CompletableFuture<Void> banMember(Member member, String reason, Member banningMember, Message message) {
        BanLog banLog = BanLog
                .builder()
                .bannedUser(member.getUser())
                .banningMember(banningMember)
                .commandMessage(message)
                .reason(reason)
                .build();
        CompletableFuture<Void> banFuture = banUser(member.getGuild(), member.getUser(), reason);
        CompletableFuture<Void> messageFuture = sendBanLogMessage(banLog, member.getGuild().getIdLong(), BAN_LOG_TEMPLATE);
        return CompletableFuture.allOf(banFuture, messageFuture);
    }

    @Override
    public CompletableFuture<Void> banUser(User user, String reason, Member banningMember, Message message) {
        BanLog banLog = BanLog
                .builder()
                .bannedUser(user)
                .banningMember(banningMember)
                .commandMessage(message)
                .reason(reason)
                .build();
        Guild guild = banningMember.getGuild();
        CompletableFuture<Void> banFuture = banUser(guild, user, reason);
        CompletableFuture<Void> messageFuture = sendBanLogMessage(banLog, guild.getIdLong(), BAN_LOG_TEMPLATE);
        return CompletableFuture.allOf(banFuture, messageFuture);
    }

    @Override
    public CompletableFuture<Void> unBanUser(User user, Member unBanningMember) {
        Guild guild = unBanningMember.getGuild();
        UnBanLog banLog = UnBanLog
                .builder()
                .bannedUser(user)
                .unBanningMember(unBanningMember)
                .build();
        return unBanUser(guild, user)
                .thenCompose(unused -> self.sendUnBanLogMessage(banLog, guild.getIdLong(), UN_BAN_LOG_TEMPLATE));
    }

    public CompletableFuture<Void> sendBanLogMessage(BanLog banLog, Long guildId, String template) {
        CompletableFuture<Void> completableFuture;
        MessageToSend banLogMessage = templateService.renderEmbedTemplate(template, banLog, guildId);
        log.debug("Sending ban log message in guild {}.", guildId);
        List<CompletableFuture<Message>> notificationFutures = postTargetService.sendEmbedInPostTarget(banLogMessage, ModerationPostTarget.BAN_LOG, guildId);
        completableFuture = FutureUtils.toSingleFutureGeneric(notificationFutures);
        return completableFuture;
    }

    public CompletableFuture<Void> sendUnBanLogMessage(UnBanLog banLog, Long guildId, String template) {
        CompletableFuture<Void> completableFuture;
        MessageToSend banLogMessage = templateService.renderEmbedTemplate(template, banLog, guildId);
        log.debug("Sending unban log message in guild {}.", guildId);
        List<CompletableFuture<Message>> notificationFutures = postTargetService.sendEmbedInPostTarget(banLogMessage, ModerationPostTarget.UN_BAN_LOG, guildId);
        completableFuture = FutureUtils.toSingleFutureGeneric(notificationFutures);
        return completableFuture;
    }

    private CompletableFuture<Void> banUser(Guild guild, User user, String reason) {
        log.info("Banning user {} in guild {}.", user.getIdLong(), guild.getId());
        return guild.ban(user, 0, reason).submit();
    }

    private CompletableFuture<Void> unBanUser(Guild guild, User user) {
        log.info("Unbanning user {} in guild {}.", user.getIdLong(), guild.getId());
        return guild.unban(user).submit();
    }
}
