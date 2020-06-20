package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.config.posttargets.ModerationPostTarget;
import dev.sheldan.abstracto.moderation.models.template.commands.KickLogModel;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
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
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void kickMember(Member member, String reason, KickLogModel kickLogModel)  {
        Guild guild = member.getGuild();
        log.info("Kicking user {} from guild {}", member.getUser().getIdLong(), guild.getIdLong());
        guild.kick(member, reason).queue();
        this.sendKickLog(kickLogModel);
    }

    private void sendKickLog(KickLogModel kickLogModel)  {
        MessageToSend warnLogMessage = templateService.renderEmbedTemplate(KICK_LOG_TEMPLATE, kickLogModel);
        postTargetService.sendEmbedInPostTarget(warnLogMessage, ModerationPostTarget.KICK_LOG, kickLogModel.getServer().getId());
    }
}
