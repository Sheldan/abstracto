package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberKickedModel;
import dev.sheldan.abstracto.core.models.listener.MemberTimeoutUpdatedModel;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.utils.CompletableFutureMap;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class AsyncMemberKickedListenerBean extends ListenerAdapter {
    @Autowired(required = false)
    private List<AsyncMemberKickedListener> listenerList;

    @Autowired
    @Qualifier("memberKickedListenerExecutor")
    private TaskExecutor memberKickedTaskExecutor;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private UserService userService;

    @Override
    public void onGuildAuditLogEntryCreate(@Nonnull GuildAuditLogEntryCreateEvent event) {
        if(listenerList == null) return;
        if(event.getEntry().getType().equals(ActionType.KICK)) {
            log.info("Handling kick audit log entry created for user {} in server {}.", event.getEntry().getTargetIdLong(), event.getGuild().getIdLong());
            CompletableFutureMap<Long, User> longUserCompletableFutureMap = userService.retrieveUsersMapped(Arrays.asList(event.getEntry().getTargetIdLong(), event.getEntry().getUserIdLong()));
            longUserCompletableFutureMap.getMainFuture().thenAccept(avoid -> {
                User kickedUser = longUserCompletableFutureMap.getElement(event.getEntry().getTargetIdLong());
                User kickingUser = longUserCompletableFutureMap.getElement(event.getEntry().getUserIdLong());
                MemberKickedModel model = getModel(event, kickedUser, kickingUser);
                listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, memberKickedTaskExecutor));
            }).exceptionally(throwable -> {
                log.warn("Failed to fetch users {} or {} for kicked event.", event.getEntry().getTargetIdLong(), event.getEntry().getUserIdLong(), throwable);
                MemberKickedModel model = getModel(event, null, null);
                listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, memberKickedTaskExecutor));
                return null;
            });
        }
    }

    private MemberKickedModel getModel(GuildAuditLogEntryCreateEvent event, User kickedUser, User kickingUser) {
        ServerUser kickededServerUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getEntry().getTargetIdLong())
                .build();
        ServerUser kickingServerUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getEntry().getUserIdLong())
                .build();
        return MemberKickedModel
                .builder()
                .kickedServerUser(kickededServerUser)
                .kickingServerUser(kickingServerUser)
                .kickedUser(kickedUser)
                .kickingUser(kickingUser)
                .guild(event.getGuild())
                .reason(event.getEntry().getReason())
                .build();
    }

}
