package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.KickLogModel;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KickServiceBean implements KickService {

    public static final String KICK_LOG_TEMPLATE = "kick_log";
    @Autowired
    private Bot bot;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void kickMember(Member member, String reason, KickLogModel kickLogModel) {
        Guild guildById = bot.getInstance().getGuildById(member.getGuild().getIdLong());
        if(guildById != null) {
            guildById.kick(member, reason).queue();
            this.sendKickLog(kickLogModel);
        } else {
            log.warn("Failed to kick member {} from guild {}. Guild was not found.", member.getId(), member.getGuild().getId());
        }
    }

    private void sendKickLog(KickLogModel kickLogModel) {
        String warnLogMessage = templateService.renderContextAwareTemplate(KICK_LOG_TEMPLATE, kickLogModel);
        postTargetService.sendTextInPostTarget(warnLogMessage, PostTarget.WARN_LOG, kickLogModel.getServer().getId());
    }
}
