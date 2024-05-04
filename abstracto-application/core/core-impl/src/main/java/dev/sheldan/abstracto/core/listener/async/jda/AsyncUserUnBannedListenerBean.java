package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.UserUnBannedModel;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.utils.CompletableFutureMap;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.GuildAuditLogEntryCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class AsyncUserUnBannedListenerBean extends ListenerAdapter {
    @Autowired(required = false)
    private List<AsyncUserUnBannedListener> listenerList;

    @Autowired
    @Qualifier("userUnBannedListenerExecutor")
    private TaskExecutor leaveListenerExecutor;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private UserService userService;

    @Override
    public void onGuildAuditLogEntryCreate(@Nonnull GuildAuditLogEntryCreateEvent event) {
        if(listenerList == null) return;
        if(event.getEntry().getType().equals(ActionType.UNBAN)) {
            log.info("Handling unBan audit log entry created for user {} in server {}.", event.getEntry().getTargetIdLong(), event.getGuild().getIdLong());
            CompletableFutureMap<Long, User> longUserCompletableFutureMap = userService.retrieveUsersMapped(Arrays.asList(event.getEntry().getTargetIdLong(), event.getEntry().getUserIdLong()));
            longUserCompletableFutureMap.getMainFuture().thenAccept(avoid -> {
                User unBannedUser = longUserCompletableFutureMap.getElement(event.getEntry().getTargetIdLong());
                User unBanningUser = longUserCompletableFutureMap.getElement(event.getEntry().getUserIdLong());
                UserUnBannedModel model = getModel(event, unBannedUser, unBanningUser);
                listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, leaveListenerExecutor));
            }).exceptionally(throwable -> {
                log.warn("Failed to fetch users {} or {} for unbanned event.", event.getEntry().getTargetIdLong(), event.getEntry().getUserIdLong(), throwable);
                UserUnBannedModel model = getModel(event, null, null);
                listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, leaveListenerExecutor));
                return null;
            });
        }
    }

    private UserUnBannedModel getModel(GuildAuditLogEntryCreateEvent event, User unBannedUser, User unBanningUser) {
        ServerUser unBannedServerUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getEntry().getTargetIdLong())
                .build();
        ServerUser unBanningServerUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getEntry().getUserIdLong())
                .build();
        return UserUnBannedModel
                .builder()
                .unBannedServerUser(unBannedServerUser)
                .unBanningUser(unBanningUser)
                .unBanningServerUser(unBanningServerUser)
                .guild(event.getGuild())
                .reason(event.getEntry().getReason())
                .unBannedUser(unBannedUser)
                .build();
    }
}
