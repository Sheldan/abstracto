package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.UserUnBannedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
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

    @Override
    public void onGuildUnban(@Nonnull GuildUnbanEvent event) {
        if(listenerList == null) return;
        UserUnBannedModel model = getModel(event);
        listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, leaveListenerExecutor));
    }

    private UserUnBannedModel getModel(GuildUnbanEvent event) {
        ServerUser serverUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getUser().getIdLong())
                .isBot(event.getUser().isBot())
                .build();
        return UserUnBannedModel
                .builder()
                .unbannedUser(serverUser)
                .guild(event.getGuild())
                .user(event.getUser())
                .build();
    }
}
