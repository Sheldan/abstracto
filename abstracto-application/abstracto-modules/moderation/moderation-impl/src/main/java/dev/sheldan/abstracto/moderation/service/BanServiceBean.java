package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.PostTarget;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.BanLog;
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
    @Autowired
    private Bot bot;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void banMember(Member member, String reason) {
        Guild guild = bot.getInstance().getGuildById(member.getGuild().getIdLong());
        if(guild != null) {
            log.info("Banning user {} in guild {}.", member.getId(), guild.getIdLong());
            guild.ban(member, 0, reason).queue();
        } else {
            log.warn("Guild id {} from member was not found.", member.getGuild().getIdLong());
        }
    }

    @Override
    public void sendBanLog(BanLog banLog) {
        String warnLogMessage = templateService.renderTemplate(BAN_LOG_TEMPLATE, banLog);
        postTargetService.sendTextInPostTarget(warnLogMessage, PostTarget.WARN_LOG, banLog.getServer().getId());
    }
}
