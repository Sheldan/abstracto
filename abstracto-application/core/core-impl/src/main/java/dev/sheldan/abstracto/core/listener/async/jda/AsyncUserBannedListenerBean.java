package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.UserBannedModel;
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
public class AsyncUserBannedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncUserBannedListener> listenerList;

    @Autowired
    @Qualifier("userBannedListenerExecutor")
    private TaskExecutor leaveListenerExecutor;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private UserService userService;

    @Override
    public void onGuildAuditLogEntryCreate(@Nonnull GuildAuditLogEntryCreateEvent event) {
        if(listenerList == null) return;
        if(event.getEntry().getType().equals(ActionType.BAN)) {
            log.info("Handling ban audit log entry created for user {} in server {}.", event.getEntry().getTargetIdLong(), event.getGuild().getIdLong());
            CompletableFutureMap<Long, User> longUserCompletableFutureMap = userService.retrieveUsersMapped(Arrays.asList(event.getEntry().getTargetIdLong(), event.getEntry().getUserIdLong()));
            longUserCompletableFutureMap.getMainFuture().thenAccept(avoid -> {
                User bannedUser = longUserCompletableFutureMap.getElement(event.getEntry().getTargetIdLong());
                User banningUser = longUserCompletableFutureMap.getElement(event.getEntry().getUserIdLong());
                UserBannedModel model = getModel(event, bannedUser, banningUser);
                listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, leaveListenerExecutor));
            }).exceptionally(throwable -> {
                log.warn("Failed to fetch users {} or {} for banned event.", event.getEntry().getTargetIdLong(), event.getEntry().getUserIdLong(), throwable);
                UserBannedModel model = getModel(event, null, null);
                listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, leaveListenerExecutor));
                return null;
            });
        }
    }

    private UserBannedModel getModel(GuildAuditLogEntryCreateEvent event, User bannedUser, User banningUser) {
        ServerUser bannedServerUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getEntry().getTargetIdLong())
                .build();
        ServerUser banningServerUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getEntry().getUserIdLong())
                .build();
        return UserBannedModel
                .builder()
                .bannedServerUser(bannedServerUser)
                .banningUser(banningUser)
                .banningServerUser(banningServerUser)
                .guild(event.getGuild())
                .reason(event.getEntry().getReason())
                .bannedUser(bannedUser)
                .build();
    }
}
