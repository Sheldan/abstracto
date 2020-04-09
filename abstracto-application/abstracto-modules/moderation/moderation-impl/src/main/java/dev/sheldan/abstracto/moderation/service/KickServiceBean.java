package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.service.Bot;
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
    private static final String WARN_LOG_TARGET = "warnLog";
    @Autowired
    private Bot bot;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void kickMember(Member member, String reason)  {
        Optional<Guild> guildById = bot.getGuildById(member.getGuild().getIdLong());
        if(guildById.isPresent()) {
            guildById.get().kick(member, reason).queue();
        } else {
            log.warn("Not able to kick. Guild {} not found.", member.getGuild().getIdLong());
            throw new GuildException(String.format("Not able to kick %s. Guild %s not found", member.getIdLong(), member.getGuild().getIdLong()));
        }
    }

    @Override
    public void sendKickLog(KickLogModel kickLogModel)  {
        String warnLogMessage = templateService.renderTemplate(KICK_LOG_TEMPLATE, kickLogModel);
        postTargetService.sendTextInPostTarget(warnLogMessage, WARN_LOG_TARGET, kickLogModel.getServer().getId());
    }
}
