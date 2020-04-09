package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.command.service.UserService;
import dev.sheldan.abstracto.core.exception.UserException;
import dev.sheldan.abstracto.core.models.AUserInAServer;
import dev.sheldan.abstracto.core.models.dto.ServerDto;
import dev.sheldan.abstracto.core.models.dto.UserDto;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.moderation.converter.WarnConverter;
import dev.sheldan.abstracto.moderation.models.dto.WarnDto;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnLogModel;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementServiceBean;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnNotificationModel;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.service.TemplateService;
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
    private UserService userService;

    @Autowired
    private WarnManagementServiceBean warnManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private Bot bot;

    private static final String WARN_LOG_TEMPLATE = "warn_log";
    private static final String WARN_NOTIFICATION_TEMPLATE = "warn_notification";

    @Autowired
    private WarnConverter warnConverter;


    @Override
    public WarnDto warnUser(UserInServerDto warnedAUserInAServer, UserInServerDto warningAUserInAServer, String reason)  {
        UserDto warningAUser = warningAUserInAServer.getUser();
        UserDto warnedAUser = warnedAUserInAServer.getUser();
        ServerDto serverOfWarning = warnedAUserInAServer.getServer();
        log.info("User {} is warning {} in server {} because of {}", warningAUser.getId(), warnedAUser.getId(), serverOfWarning.getId(), reason);
        AUserInAServer warnedUserInAServerDb = AUserInAServer.builder().userInServerId(warnedAUserInAServer.getUserInServerId()).build();
        AUserInAServer warningUserInAServerDb = AUserInAServer.builder().userInServerId(warnedAUserInAServer.getUserInServerId()).build();
        WarnDto warning = warnManagementService.createWarning(warnedUserInAServerDb, warningUserInAServerDb, reason);
        JDA instance = bot.getInstance();
        User userBeingWarned = instance.getUserById(warnedAUser.getId());
        Optional<Guild> guildById = bot.getGuildById(serverOfWarning.getId());
        String guildName = "<defaultName>";
        if(guildById.isPresent()) {
            guildName = guildById.get().getName();
        }
        WarnNotificationModel warnNotificationModel = WarnNotificationModel.builder().warning(warnConverter.convertFromWarnDto(warning)).serverName(guildName).build();
        if(userBeingWarned != null) {
            String warnLogMessage = templateService.renderTemplate(WARN_NOTIFICATION_TEMPLATE, warnNotificationModel);
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
        return warning;
    }

    @Override
    public WarnDto warnUser(Member warnedMember, Member warningMember, String reason)  {
        UserInServerDto warnedAUser = userService.loadUser(warnedMember.getGuild().getIdLong(), warnedMember.getIdLong());
        UserInServerDto warningAUser = userService.loadUser(warningMember.getGuild().getIdLong(), warningMember.getIdLong());
        return this.warnUser(warnedAUser, warningAUser, reason);
    }

    @Override
    public void sendWarnLog(WarnLogModel warnLogModel)  {
        String warnLogMessage = templateService.renderTemplate(WARN_LOG_TEMPLATE, warnLogModel);
        postTargetService.sendTextInPostTarget(warnLogMessage, WARN_LOG_TARGET, warnLogModel.getServer().getId());
        MessageToSend message = templateService.renderEmbedTemplate("warn_log", warnLogModel);
        postTargetService.sendEmbedInPostTarget(message, WARN_LOG_TARGET, warnLogModel.getServer().getId());
    }
}
