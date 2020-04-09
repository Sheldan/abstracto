package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.GuildException;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.commands.BanIdLogModel;
import dev.sheldan.abstracto.moderation.models.template.commands.BanLogModel;
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
    private static final String BAN_LOG_TARGET = "banLog";
    private static final String BAN_ID_LOG_TEMPLATE = "banid_log";

    @Autowired
    private Bot bot;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Override
    public void banMember(Member member, String reason) {
        this.banUser(member.getGuild().getIdLong(), member.getIdLong(), reason);
    }

    @Override
    public void banMember(Long guildId, Long userId, String reason) {
        this.banUser(guildId, userId, reason);
    }

    @Override
    public void sendBanLog(BanLogModel banLogModel) {
        String warnLogMessage = templateService.renderTemplate(BAN_LOG_TEMPLATE, banLogModel);
        postTargetService.sendTextInPostTarget(warnLogMessage, BAN_LOG_TARGET, banLogModel.getGuild().getIdLong());
    }

    @Override
    public void sendBanIdLog(BanIdLogModel banIdLogModel) {
        String warnLogMessage = templateService.renderTemplate(BAN_ID_LOG_TEMPLATE, banIdLogModel);
        postTargetService.sendTextInPostTarget(warnLogMessage, BAN_LOG_TARGET, banIdLogModel.getGuild().getIdLong());
    }

    private void banUser(Long guildId, Long userId, String reason) {
        Optional<Guild> guildByIdOptional = bot.getGuildById(guildId);
        if(guildByIdOptional.isPresent()) {
            log.info("Banning user {} in guild {}.", userId, guildId);
            guildByIdOptional.get().ban(userId.toString(), 0, reason).queue();
        } else {
            log.warn("Guild {} not found. Not able to ban user {}", guildId, userId);
            throw new GuildException(String.format("Guild %s not found. Not able to ban user %s", guildId, userId));
        }
    }
}
