package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.NotFoundException;
import dev.sheldan.abstracto.core.models.ServerContext;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.TemplateService;
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
    private static final String BAN_LOG_TARGET = "banLog";
    @Autowired
    private Bot bot;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void banMember(Member member, String reason, ServerContext banLog) {
        this.banUser(member.getGuild().getIdLong(), member.getIdLong(), reason);
        String warnLogMessage = templateService.renderContextAwareTemplate(BAN_LOG_TEMPLATE, banLog);
        postTargetService.sendTextInPostTarget(warnLogMessage, BAN_LOG_TARGET, banLog.getServer().getId());
    }

    @Override
    public void banMember(Long guildId, Long userId, String reason, ServerContext banIdLog) {
        banUser(guildId, userId, reason);
        String warnLogMessage = templateService.renderContextAwareTemplate(BAN_ID_LOG_TEMPLATE, banIdLog);
        postTargetService.sendTextInPostTarget(warnLogMessage, BAN_LOG_TARGET, guildId);
    }

    private void banUser(Long guildId, Long userId, String reason) {
        Optional<Guild> guildByIdOptional = bot.getGuildById(guildId);
        if(guildByIdOptional.isPresent()) {
            log.info("Banning user {} in guild {}.", userId, guildId);
            guildByIdOptional.get().ban(userId.toString(), 0, reason).queue();
        } else {
            log.warn("Guild {} not found. Not able to ban user {}", guildId, userId);
            throw new NotFoundException(String.format("Guild %s not found. Not able to ban user %s", guildId, userId));
        }
    }
}
