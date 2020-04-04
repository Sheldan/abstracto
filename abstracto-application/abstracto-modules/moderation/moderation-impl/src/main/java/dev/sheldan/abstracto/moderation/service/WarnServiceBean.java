package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.exception.UserException;
import dev.sheldan.abstracto.core.models.ServerContext;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.embed.MessageToSend;
import dev.sheldan.abstracto.moderation.models.template.WarnLog;
import dev.sheldan.abstracto.moderation.models.template.WarnNotification;
import dev.sheldan.abstracto.moderation.models.Warning;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class WarnServiceBean implements WarnService {

    public static final String WARN_LOG_TARGET = "warnLog";
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
    public void warnUser(AUserInAServer warnedAUserInAServer, AUserInAServer warningAUserInAServer, String reason, WarnLog warnLog)  {
        AUser warningAUser = warningAUserInAServer.getUserReference();
        AUser warnedAUser = warnedAUserInAServer.getUserReference();
        AServer serverOfWarning = warnedAUserInAServer.getServerReference();
        log.info("User {} is warning {} in server {} because of {}", warningAUser.getId(), warnedAUser.getId(), serverOfWarning.getId(), reason);
        Warning warning = warnManagementService.createWarning(warnedAUserInAServer, warningAUserInAServer, reason);
        JDA instance = bot.getInstance();
        User userBeingWarned = instance.getUserById(warnedAUser.getId());
        Optional<Guild> guildById = bot.getGuildById(serverOfWarning.getId());
        String guildName = "<defaultName>";
        if(guildById.isPresent()) {
            guildName = guildById.get().getName();
        }
        warnLog.setWarning(warning);
        this.sendWarnLog(warnLog);
        WarnNotification warnNotification = WarnNotification.builder().warning(warning).serverName(guildName).build();
        if(userBeingWarned != null) {
            String warnLogMessage = templateService.renderTemplate(WARN_NOTIFICATION_TEMPLATE, warnNotification);
            CompletableFuture<Message> messageFuture = new CompletableFuture<>();

            // TODO the person executing this, is unaware that the message failed
            userBeingWarned.openPrivateChannel().queue(privateChannel -> {
                log.info("Messaging user {} about warn {}", warnedAUser.getId(), warning.getId());
                privateChannel.sendMessage(warnLogMessage).queue(messageFuture::complete, messageFuture::completeExceptionally);
            });

            messageFuture.exceptionally(e -> {
                log.warn("Failed to send message. ", e);
                return null;
            });
        } else {
            log.warn("Unable to find user {} in guild {} to warn.", warnedAUser.getId(), serverOfWarning.getId());
            throw new UserException(String.format("Unable to find user %s.", warnedAUser.getId()));
        }
    }

    @Override
    public void warnUser(Member warnedMember, Member warningMember, String reason, WarnLog warnLog)  {
        AUserInAServer warnedAUser = userManagementService.loadUser(warnedMember);
        AUserInAServer warningAUser = userManagementService.loadUser(warningMember);
        this.warnUser(warnedAUser, warningAUser, reason, warnLog);
    }

    private void sendWarnLog(ServerContext warnLogModel)  {
        String warnLogMessage = templateService.renderContextAwareTemplate(WARN_LOG_TEMPLATE, warnLogModel);
        postTargetService.sendTextInPostTarget(warnLogMessage, WARN_LOG_TARGET, warnLogModel.getServer().getId());
        MessageToSend message = templateService.renderEmbedTemplate("warn_log", warnLogModel);
        postTargetService.sendEmbedInPostTarget(message, WARN_LOG_TARGET, warnLogModel.getServer().getId());
    }
}
