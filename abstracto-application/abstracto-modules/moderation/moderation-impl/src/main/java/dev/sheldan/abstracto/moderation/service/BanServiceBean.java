package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.posttargets.ModerationPostTarget;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class BanServiceBean implements BanService {

    public static final String BAN_LOG_TEMPLATE = "ban_log";
    public static final String BAN_ID_LOG_TEMPLATE = "banId_log";

    @Autowired
    private BotService botService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public CompletableFuture<Void> banMember(Member member, String reason, ServerContext banLog) {
        CompletableFuture<Void> banFuture = banUser(member.getGuild(), member.getIdLong(), reason);
        MessageToSend banLogMessage = templateService.renderEmbedTemplate(BAN_LOG_TEMPLATE, banLog);
        List<CompletableFuture<Message>> notificationFutures = postTargetService.sendEmbedInPostTarget(banLogMessage, ModerationPostTarget.BAN_LOG, member.getGuild().getIdLong());
        return CompletableFuture.allOf(banFuture, FutureUtils.toSingleFutureGeneric(notificationFutures));
    }

    @Override
    public CompletableFuture<Void> banMember(Long guildId, Long userId, String reason, ServerContext banIdLog) {
        CompletableFuture<Void> banFuture = banUser(guildId, userId, reason);
        MessageToSend banLogMessage = templateService.renderEmbedTemplate(BAN_ID_LOG_TEMPLATE, banIdLog);
        List<CompletableFuture<Message>> notificationFutures = postTargetService.sendEmbedInPostTarget(banLogMessage, ModerationPostTarget.BAN_LOG, guildId);
        return CompletableFuture.allOf(banFuture, FutureUtils.toSingleFutureGeneric(notificationFutures));
    }

    private CompletableFuture<Void> banUser(Long guildId, Long userId, String reason) {
        Optional<Guild> guildByIdOptional = botService.getGuildById(guildId);
        if(guildByIdOptional.isPresent()) {
            return banUser(guildByIdOptional.get(), userId, reason);
        } else {
            log.warn("Guild {} not found. Not able to ban user {}", guildId, userId);
            throw new GuildNotFoundException(guildId);
        }
    }

    private CompletableFuture<Void> banUser(Guild guild, Long userId, String reason) {
        log.info("Banning user {} in guild {}.", userId, guild.getId());
        return guild.ban(userId.toString(), 0, reason).submit();
    }
}
