package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerContext;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.BanIdLog;
import dev.sheldan.abstracto.moderation.models.template.BanLog;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BanServiceBean implements BanService {

    public static final String BAN_LOG_TEMPLATE = "ban_log";
    public static final String BAN_ID_LOG_TEMPLATE = "banid_log";
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
        postTargetService.sendTextInPostTarget(warnLogMessage, PostTarget.BAN_LOG, banLog.getServer().getId());
    }

    @Override
    public void banMember(Long guildId, Long userId, String reason, ServerContext banIdLog) {
        banUser(guildId, userId, reason);
        String warnLogMessage = templateService.renderContextAwareTemplate(BAN_ID_LOG_TEMPLATE, banIdLog);
        postTargetService.sendTextInPostTarget(warnLogMessage, PostTarget.BAN_LOG, guildId);
    }

    private void banUser(Long guildId, Long userId, String reason) {
        Guild guildById = bot.getInstance().getGuildById(guildId);
        if(guildById != null) {
            log.info("Banning user {} in guild {}.", userId, guildId);
            guildById.ban(userId.toString(), 0, reason).queue();
        } else {
            log.warn("Guild id {} from member was not found.", guildId);
        }
    }
}
