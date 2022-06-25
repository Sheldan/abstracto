package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMemberTimeoutUpdatedListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberTimeoutUpdatedModel;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.config.posttarget.MutingPostTarget;
import dev.sheldan.abstracto.moderation.model.template.command.MuteListenerModel;
import dev.sheldan.abstracto.moderation.service.MuteServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class MemberTimeoutListener implements AsyncMemberTimeoutUpdatedListener {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private MemberTimeoutListener self;

    @Autowired
    private MemberService memberService;

    @Override
    public DefaultListenerResult execute(MemberTimeoutUpdatedModel model) {
        Guild guild = model.getGuild();
        guild.retrieveAuditLogs()
                .type(ActionType.MEMBER_UPDATE)
                .limit(10)
                .queue(auditLogEntries -> {
                    CompletableFuture<Void> notificationFuture = null;
                    if(auditLogEntries.isEmpty()) {
                        log.info("Did not find recent timeouts in guild {}.", model.getServerId());
                        notificationFuture = self.sendMutingUpdateNotification(model, null);
                    } else {
                        Optional<AuditLogEntry> timeoutEntryOptional = auditLogEntries
                                .stream()
                                .filter(auditLogEntry -> auditLogEntry.getChangeByKey(AuditLogKey.MEMBER_TIME_OUT) != null
                                        && auditLogEntry.getTargetIdLong() == model.getTimeoutUser().getUserId())
                                .findFirst();
                        if(timeoutEntryOptional.isPresent()) {
                            AuditLogEntry auditLogEntry = timeoutEntryOptional.get();
                            User responsibleUser = auditLogEntry.getUser();
                            if(guild.getSelfMember().getIdLong() != responsibleUser.getIdLong()) {
                                notificationFuture = self.sendMutingUpdateNotification(model, auditLogEntry);
                            }
                        } else {
                            notificationFuture = self.sendMutingUpdateNotification(model, null);
                        }
                    }
                    if(notificationFuture != null) {
                        notificationFuture.thenAccept(unused -> {
                            log.info("Sent notification about timeout change {} -> {} of user {} in server {}.",
                                    model.getOldTimeout(), model.getNewTimeout(), model.getTimeoutUser().getUserId(), model.getServerId());
                        }).exceptionally(throwable -> {
                            log.info("Sent notification about timeout change {} -> {} of user {} in server {}.",
                                    model.getOldTimeout(), model.getNewTimeout(), model.getTimeoutUser().getUserId(), model.getServerId());
                            return null;
                        });
                    }
                });
        return DefaultListenerResult.PROCESSED;
    }

    @Transactional
    public CompletableFuture<Void> sendMutingUpdateNotification(MemberTimeoutUpdatedModel model, AuditLogEntry logEntry) {
        User responsibleUser;
        Guild guild = model.getGuild();
        CompletableFuture<Member> future;
        String reason;
        if(logEntry != null) {
            responsibleUser = logEntry.getUser();
            if(responsibleUser != null) {
                ServerUser responsibleServerUser = ServerUser
                        .builder()
                        .serverId(guild.getIdLong())
                        .isBot(responsibleUser.isBot())
                        .userId(responsibleUser.getIdLong())
                        .build();
                future = memberService.retrieveMemberInServer(responsibleServerUser);
            } else {
                future = CompletableFuture.completedFuture(null);
            }
            reason = logEntry.getReason();
        } else {
            future = CompletableFuture.completedFuture(null);
            reason = null;
        }
        CompletableFuture<Void> returningFuture = new CompletableFuture<>();
        future.whenComplete((aVoid, throwable) -> {
            try {
                MuteListenerModel muteLogModel = MuteListenerModel
                        .builder()
                        .muteTargetDate(model.getNewTimeout() != null ? model.getNewTimeout().toInstant() : null)
                        .oldMuteTargetDate(model.getOldTimeout() != null ? model.getOldTimeout().toInstant() : null)
                        .mutingUser(future.isCompletedExceptionally() ? null : future.join())
                        .mutedUser(model.getMember())
                        .reason(reason)
                        .build();
                MessageToSend message = templateService.renderEmbedTemplate(MuteServiceBean.MUTE_LOG_TEMPLATE, muteLogModel, guild.getIdLong());
                FutureUtils.toSingleFutureGeneric(postTargetService.sendEmbedInPostTarget(message, MutingPostTarget.MUTE_LOG, model.getServerId()));
                returningFuture.complete(null);
            } catch (Exception exception) {
                log.error("Failed to log timeout update  event for user {} in guild {}.", model.getTimeoutUser().getUserId(), model.getServerId(), exception);
            }
        });

        return returningFuture;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
    }
}
