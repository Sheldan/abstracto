package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberTimeoutUpdatedModel;
import dev.sheldan.abstracto.core.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AsyncMemberTimeoutListenerBean extends ListenerAdapter {
    @Autowired(required = false)
    private List<AsyncMemberTimeoutUpdatedListener> listenerList;

    @Autowired
    @Qualifier("memberTimeoutUpdatedListenerExecutor")
    private TaskExecutor memberTimeoutExecutor;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private MemberService memberService;

    @Override
    public void onGuildAuditLogEntryCreate(@Nonnull GuildAuditLogEntryCreateEvent event) {
        if(listenerList == null) return;
        if(event.getEntry().getType().equals(ActionType.MEMBER_UPDATE)) {
            AuditLogChange memberTimeoutChange = event.getEntry().getChangeByKey(AuditLogKey.MEMBER_TIME_OUT);
            if(memberTimeoutChange != null) {
                CompletableFuture<Member> targetMemberFuture = memberService.retrieveMemberInServer(ServerUser.fromId(event.getGuild().getIdLong(), event.getEntry().getTargetIdLong()));
                CompletableFuture<Member> mutingMemberFuture = memberService.retrieveMemberInServer(ServerUser.fromId(event.getGuild().getIdLong(), event.getEntry().getUserIdLong()));
                CompletableFuture.allOf(targetMemberFuture, mutingMemberFuture).whenComplete((avoid, throwable) -> {
                    executeListeners(memberTimeoutChange, event, targetMemberFuture.join(), mutingMemberFuture.join());
                }).exceptionally(throwable -> {
                    Long memberId = event.getEntry().getTargetIdLong();
                    Long serverId = event.getGuild().getIdLong();
                    log.warn("Failed to load member {} for member update audit log in server {}.", memberId, serverId, throwable);
                    executeListeners(memberTimeoutChange, event, null, null);
                    return null;
                });

            }
        }
    }

   private void executeListeners(AuditLogChange change, GuildAuditLogEntryCreateEvent event, Member mutedMember, Member mutingMember) {
       DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
       OffsetDateTime timeoutAfter = change.getNewValue() != null ? OffsetDateTime.parse(change.getNewValue(), timeFormatter) : null;
       OffsetDateTime timeoutBefore = change.getOldValue() != null ? OffsetDateTime.parse(change.getOldValue(), timeFormatter) : null;
       String reason = event.getEntry().getReason();
       Long serverId = event.getGuild().getIdLong();
       MemberTimeoutUpdatedModel model =  MemberTimeoutUpdatedModel
               .builder()
               .oldTimeout(timeoutBefore)
               .newTimeout(timeoutAfter)
               .responsibleUserId(event.getEntry().getUserIdLong())
               .mutedMember(mutedMember)
               .mutingMember(mutingMember)
               .reason(reason)
               .guild(event.getGuild())
               .mutingUser(ServerUser.fromId(serverId, event.getEntry().getUserIdLong()))
               .mutedUser(ServerUser.fromId(serverId, event.getEntry().getTargetIdLong()))
               .build();
       listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, memberTimeoutExecutor));
   }
}
