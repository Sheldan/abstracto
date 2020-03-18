package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.AServer;
import dev.sheldan.abstracto.core.models.AUser;
import dev.sheldan.abstracto.moderation.models.WarnLog;
import dev.sheldan.abstracto.moderation.models.WarnNotification;
import dev.sheldan.abstracto.moderation.models.Warning;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.core.management.ServerManagementService;
import dev.sheldan.abstracto.core.management.UserManagementService;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.core.models.PostTarget;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WarnServiceBean implements WarnService {

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private WarnManagementService warnManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private Bot bot;

    private static final String WARN_LOG_TEMPLATE = "warn_log";
    private static final String WARN_NOTIFICATION_TEMPLATE = "warn_notification";

    @Override
    public Warning warnUser(AUserInAServer warnedAUserInAServer, AUserInAServer warningAUserInAServer, String reason) {
        AUser warningAUser = warningAUserInAServer.getUserReference();
        AUser warnedAUser = warnedAUserInAServer.getUserReference();
        AServer serverOfWarning = warnedAUserInAServer.getServerReference();
        log.info("User {} is warning {} in server {} because of {}", warningAUser.getId(), warnedAUser.getId(), serverOfWarning.getId(), reason);
        Warning warning = warnManagementService.createWarning(warnedAUserInAServer, warningAUserInAServer, reason);
        JDA instance = bot.getInstance();
        User userBeingWarned = instance.getUserById(warnedAUser.getId());
        Guild guildById = instance.getGuildById(serverOfWarning.getId());
        String guildName = "<defaultName>";
        if(guildById != null) {
            guildName = guildById.getName();
        }
        WarnNotification warnNotification = WarnNotification.builder().warning(warning).serverName(guildName).build();
        if(userBeingWarned != null) {
            String warnLogMessage = templateService.renderTemplate(WARN_NOTIFICATION_TEMPLATE, warnNotification);
            userBeingWarned.openPrivateChannel().queue(privateChannel -> {
                log.info("Messaging user {} about warn {}", warnedAUser.getId(), warning.getId());
                privateChannel.sendMessage(warnLogMessage).queue();
            });
        } else {
            log.warn("Unable to find user {} in guild {} to warn.", warnedAUser.getId(), serverOfWarning.getId());
        }
        return warning;
    }

    @Override
    public Warning warnUser(Member warnedMember, Member warningMember, String reason) {
        AUserInAServer warnedAUser = userManagementService.loadUser(warnedMember);
        AUserInAServer warningAUser = userManagementService.loadUser(warningMember);
        return this.warnUser(warnedAUser, warningAUser, reason);
    }

    @Override
    public void sendWarnLog(WarnLog warnLogModel) {
        String warnLogMessage = templateService.renderTemplate(WARN_LOG_TEMPLATE, warnLogModel);
        postTargetService.sendTextInPostTarget(warnLogMessage, PostTarget.WARN_LOG, warnLogModel.getServer().getId());
    }
}
