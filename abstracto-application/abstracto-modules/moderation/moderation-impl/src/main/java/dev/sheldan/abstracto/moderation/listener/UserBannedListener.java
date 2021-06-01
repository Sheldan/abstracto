package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncUserBannedListener;
import dev.sheldan.abstracto.core.models.listener.UserBannedModel;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.moderation.model.template.listener.UserBannedListenerModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class UserBannedListener implements AsyncUserBannedListener {

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private UserBannedListener self;

    private static final String USER_BANNED_NOTIFICATION_TEMPLATE = "userBanned_listener_notification";

    @Override
    public DefaultListenerResult execute(UserBannedModel model) {
        model.getGuild()
                .retrieveAuditLogs()
                .type(ActionType.BAN)
                .limit(5)
                .queue(auditLogEntries -> {
                    if(auditLogEntries.isEmpty()) {
                        log.info("Did not find recent bans in guild {}.", model.getServerId());
                        return;
                    }
                    Optional<AuditLogEntry> banEntryOptional = auditLogEntries
                            .stream()
                            .filter(auditLogEntry -> auditLogEntry.getTargetIdLong() == model.getBannedUser().getUserId())
                            .findFirst();
                    if(banEntryOptional.isPresent()) {
                        AuditLogEntry auditLogEntry = banEntryOptional.get();
                        if(!model.getGuild().getJDA().getSelfUser().equals(auditLogEntry.getUser())) {
                            self.sendBannedNotification(model.getUser(), auditLogEntry.getUser(), auditLogEntry.getReason(), model.getServerId());
                        }
                    } else {
                        log.info("Did not find the banned user in the most recent bans for guild {}. Not adding audit log information.", model.getServerId());
                        self.sendBannedNotification(model.getUser(), null, null, model.getServerId());
                    }
                });
        return DefaultListenerResult.PROCESSED;
    }

    @Transactional
    public CompletableFuture<Void> sendBannedNotification(User bannedUser, User banningUser, String reason, Long serverId) {
        UserBannedListenerModel model = UserBannedListenerModel
                .builder()
                .bannedUser(bannedUser)
                .banningUser(banningUser)
                .reason(reason)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(USER_BANNED_NOTIFICATION_TEMPLATE, model, serverId);
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, ModerationPostTarget.BAN_LOG, serverId));
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }
}
