package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.posttargets.ModerationPostTarget;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class BanServiceBean implements BanService {

    private static final String BAN_LOG_TEMPLATE = "ban_log";
    private static final String BAN_ID_LOG_TEMPLATE = "banid_log";
    @Autowired
    private BotService botService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void banMember(Member member, String reason, ServerContext banLog) {
        this.banUser(member.getGuild().getIdLong(), member.getIdLong(), reason);
        String warnLogMessage = templateService.renderTemplate(BAN_LOG_TEMPLATE, banLog);
        postTargetService.sendTextInPostTarget(warnLogMessage, ModerationPostTarget.BAN_LOG, banLog.getServer().getId());
    }

    @Override
    public void banMember(Long guildId, Long userId, String reason, ServerContext banIdLog) {
        banUser(guildId, userId, reason);
        String warnLogMessage = templateService.renderTemplate(BAN_ID_LOG_TEMPLATE, banIdLog);
        postTargetService.sendTextInPostTarget(warnLogMessage, ModerationPostTarget.BAN_LOG, guildId);
    }

    private void banUser(Long guildId, Long userId, String reason) {
        Optional<Guild> guildByIdOptional = botService.getGuildById(guildId);
        if(guildByIdOptional.isPresent()) {
            log.info("Banning user {} in guild {}.", userId, guildId);
            guildByIdOptional.get().ban(userId.toString(), 0, reason).queue();
        } else {
            log.warn("Guild {} not found. Not able to ban user {}", guildId, userId);
            throw new GuildException(guildId);
        }
    }
}
