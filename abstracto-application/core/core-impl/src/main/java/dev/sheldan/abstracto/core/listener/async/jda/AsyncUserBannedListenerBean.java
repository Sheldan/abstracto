package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.UserBannedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

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

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        if(listenerList == null) return;
        UserBannedModel model = getModel(event);
        listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, leaveListenerExecutor));
    }

    private UserBannedModel getModel(GuildBanEvent event) {
        ServerUser serverUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getUser().getIdLong())
                .build();
        return UserBannedModel
                .builder()
                .bannedUser(serverUser)
                .guild(event.getGuild())
                .user(event.getUser())
                .build();
    }
}
