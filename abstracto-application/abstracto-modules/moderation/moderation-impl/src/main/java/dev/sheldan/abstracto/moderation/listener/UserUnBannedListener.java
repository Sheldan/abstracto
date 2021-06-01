package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncUserUnBannedListener;
import dev.sheldan.abstracto.core.models.listener.UserUnBannedModel;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.ModerationPostTarget;
import dev.sheldan.abstracto.moderation.model.template.listener.UserUnBannedListenerModel;
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
public class UserUnBannedListener implements AsyncUserUnBannedListener {
    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private UserUnBannedListener self;

    private static final String USER_UN_BANNED_NOTIFICATION_TEMPLATE = "userUnBanned_listener_notification";

    @Override
    public DefaultListenerResult execute(UserUnBannedModel model) {
        model.getGuild()
                .retrieveAuditLogs()
                .type(ActionType.UNBAN)
                .limit(5)
                .queue(auditLogEntries -> {
                    if(auditLogEntries.isEmpty()) {
                        log.info("Did not find recent bans in guild {}.", model.getServerId());
                        return;
                    }
                    Optional<AuditLogEntry> banEntryOptional = auditLogEntries
                            .stream()
                            .filter(auditLogEntry -> auditLogEntry.getTargetIdLong() == model.getUnbannedUser().getUserId())
                            .findFirst();
                    if(banEntryOptional.isPresent()) {
                        AuditLogEntry auditLogEntry = banEntryOptional.get();
                        if(!model.getGuild().getJDA().getSelfUser().equals(auditLogEntry.getUser())) {
                            self.sendUnBannedNotification(model.getUser(), auditLogEntry.getUser(), model.getServerId());
                        }
                    } else {
                        log.info("Did not find the un-banned user in the most recent un-bans for guild {}. Not adding audit log information.", model.getServerId());
                        self.sendUnBannedNotification(model.getUser(), null, model.getServerId());
                    }
                });
        return DefaultListenerResult.PROCESSED;
    }

    @Transactional
    public CompletableFuture<Void> sendUnBannedNotification(User unbannedUser, User unbanningUser, Long serverId) {
        UserUnBannedListenerModel model = UserUnBannedListenerModel
                .builder()
                .unBannedUser(unbannedUser)
                .unBanningUser(unbanningUser)
                .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(USER_UN_BANNED_NOTIFICATION_TEMPLATE, model, serverId);
        return FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(messageToSend, ModerationPostTarget.BAN_LOG, serverId));
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }
}
