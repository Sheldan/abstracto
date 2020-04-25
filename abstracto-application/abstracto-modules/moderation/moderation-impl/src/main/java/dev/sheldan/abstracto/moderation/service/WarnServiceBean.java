package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnLog;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnNotification;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import dev.sheldan.abstracto.core.service.management.UserManagementService;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WarnServiceBean implements WarnService {

    public static final String WARN_LOG_TARGET = "warnLog";

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private WarnManagementService warnManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BotService botService;

    @Autowired
    private MessageService messageService;

    private static final String WARN_LOG_TEMPLATE = "warn_log";
    private static final String WARN_NOTIFICATION_TEMPLATE = "warn_notification";

    @Override
    public Warning warnUser(AUserInAServer warnedAUserInAServer, AUserInAServer warningAUserInAServer, String reason, TextChannel feedbackChannel)  {
        FullUser warnedUser = FullUser
                .builder()
                .aUserInAServer(warnedAUserInAServer)
                .member(botService.getMemberInServer(warnedAUserInAServer))
                .build();

        FullUser warningUser = FullUser
                .builder()
                .aUserInAServer(warningAUserInAServer)
                .member(botService.getMemberInServer(warningAUserInAServer))
                .build();
       return warnUser(warnedUser, warningUser, reason, feedbackChannel);
    }

    @Override
    public Warning warnUser(Member warnedMember, Member warningMember, String reason, TextChannel feedbackChannel) {
        FullUser warnedUser = FullUser
                .builder()
                .aUserInAServer(userManagementService.loadUser(warnedMember))
                .member(warnedMember)
                .build();

        FullUser warningUser = FullUser
                .builder()
                .aUserInAServer(userManagementService.loadUser(warningMember))
                .member(warningMember)
                .build();
        return warnUser(warnedUser, warningUser, reason, feedbackChannel);
    }

    @Override
    public Warning warnUser(FullUser warnedMember, FullUser warningMember, String reason, TextChannel feedbackChannel)  {
        Guild guild = warnedMember.getMember().getGuild();
        log.info("User {} is warning {} in server {} because of {}", warnedMember.getMember().getId(), warningMember.getMember().getId(), guild.getIdLong(), reason);
        Warning warning = warnManagementService.createWarning(warnedMember.getAUserInAServer(), warningMember.getAUserInAServer(), reason);
        WarnNotification warnNotification = WarnNotification.builder().warning(warning).serverName(guild.getName()).build();
        String warnNotificationMessage = templateService.renderTemplate(WARN_NOTIFICATION_TEMPLATE, warnNotification);
        messageService.sendMessageToUser(warnedMember.getMember().getUser(), warnNotificationMessage, feedbackChannel);
        return warning;
    }

    @Override
    public void warnUserWithLog(Member warnedMember, Member warningMember, String reason, WarnLog warnLog, TextChannel feedbackChannel) {
        Warning warning = warnUser(warnedMember, warningMember, reason, feedbackChannel);
        warnLog.setWarning(warning);
        this.sendWarnLog(warnLog);
    }

    private void sendWarnLog(ServerContext warnLogModel)  {
        MessageToSend message = templateService.renderEmbedTemplate(WARN_LOG_TEMPLATE, warnLogModel);
        postTargetService.sendEmbedInPostTarget(message, WARN_LOG_TARGET, warnLogModel.getServer().getId());
    }
}
