package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.GuildNotFoundException;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.features.ModerationMode;
import dev.sheldan.abstracto.moderation.config.posttargets.ModerationPostTarget;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
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

    @Autowired
    private FeatureModeService featureModeService;

    @Override
    public CompletableFuture<Void> banMember(Member member, String reason, ServerContext banLog) {
        CompletableFuture<Void> banFuture = banUser(member.getGuild(), member.getIdLong(), reason);
        CompletableFuture<Void> messageFuture = sendBanLogMessage(banLog, member.getGuild().getIdLong(), BAN_LOG_TEMPLATE);
        return CompletableFuture.allOf(banFuture, messageFuture);
    }

    @NotNull
    public CompletableFuture<Void> sendBanLogMessage(ServerContext banLog, Long guildId, String template) {
        CompletableFuture<Void> completableFuture;
        if(featureModeService.featureModeActive(ModerationFeatures.MODERATION, guildId, ModerationMode.BAN_LOG)) {
            MessageToSend banLogMessage = templateService.renderEmbedTemplate(template, banLog);
            log.trace("Sending ban log message in guild {}.", guildId);
            List<CompletableFuture<Message>> notificationFutures = postTargetService.sendEmbedInPostTarget(banLogMessage, ModerationPostTarget.BAN_LOG, guildId);
            completableFuture = FutureUtils.toSingleFutureGeneric(notificationFutures);
        } else {
            log.trace("Feature {} has mode {} for logging disabled for server {}. Not sending notification.", ModerationFeatures.MODERATION, ModerationMode.BAN_LOG, guildId);
            completableFuture = CompletableFuture.completedFuture(null);
        }
        return completableFuture;
    }

    @Override
    public CompletableFuture<Void> banUserViaId(Long guildId, Long userId, String reason, ServerContext banIdLog) {
        CompletableFuture<Void> banFuture = banUser(guildId, userId, reason);
        CompletableFuture<Void> messageFuture = sendBanLogMessage(banIdLog, guildId, BAN_ID_LOG_TEMPLATE);
        return CompletableFuture.allOf(banFuture, messageFuture);
    }

    private CompletableFuture<Void> banUser(Long guildId, Long userId, String reason) {
        Optional<Guild> guildByIdOptional = botService.getGuildByIdOptional(guildId);
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
