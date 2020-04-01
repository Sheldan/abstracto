package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.NotFoundException;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.KickLogModel;
import dev.sheldan.abstracto.templating.TemplateService;
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
    private static final String WARN_LOG_TARGET = "warnLog";
    @Autowired
    private Bot bot;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void kickMember(Member member, String reason, KickLogModel kickLogModel) {
        Optional<Guild> guildById = bot.getGuildById(kickLogModel.getGuild().getIdLong());
        if(guildById.isPresent()) {
            guildById.get().kick(member, reason).queue();
            this.sendKickLog(kickLogModel);
        } else {
            log.warn("Not able to kick. Guild {} not found.", kickLogModel.getGuild().getIdLong());
            throw new NotFoundException(String.format("Not able to kick %s. Guild %s not found", kickLogModel.getMember().getIdLong(), kickLogModel.getGuild().getIdLong()));
        }
    }

    private void sendKickLog(KickLogModel kickLogModel) {
        String warnLogMessage = templateService.renderContextAwareTemplate(KICK_LOG_TEMPLATE, kickLogModel);
        postTargetService.sendTextInPostTarget(warnLogMessage, WARN_LOG_TARGET, kickLogModel.getServer().getId());
    }
}
