package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.commands.KickLogModel;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class KickServiceBean implements KickService {

    private static final String KICK_LOG_TEMPLATE = "kick_log";
    private static final String KICK_LOG_TARGET = "kickLog";
    @Autowired
    private BotService botService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void kickMember(Member member, String reason, KickLogModel kickLogModel)  {
        Optional<Guild> guildById = botService.getGuildById(kickLogModel.getGuild().getIdLong());
        log.info("Kicking user {} from guild {}", member.getUser().getIdLong(), member.getGuild().getIdLong());
        if(guildById.isPresent()) {
            guildById.get().kick(member, reason).queue();
            this.sendKickLog(kickLogModel);
        } else {
            log.warn("Not able to kick. Guild {} not found.", kickLogModel.getGuild().getIdLong());
            throw new GuildException(kickLogModel.getGuild().getIdLong());
        }
    }

    private void sendKickLog(KickLogModel kickLogModel)  {
        String warnLogMessage = templateService.renderTemplate(KICK_LOG_TEMPLATE, kickLogModel);
        postTargetService.sendTextInPostTarget(warnLogMessage, KICK_LOG_TARGET, kickLogModel.getServer().getId());
    }
}
