package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberLeaveModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class AsyncLeaveListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncLeaveListener> listenerList;

    @Autowired
    @Qualifier("leaveListenerExecutor")
    private TaskExecutor leaveListenerExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        if(listenerList == null) return;
        MemberLeaveModel model = getModel(event);
        listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, leaveListenerExecutor));
    }

    private MemberLeaveModel getModel(GuildMemberRemoveEvent event) {
        ServerUser serverUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getUser().getIdLong())
                .build();
        return MemberLeaveModel
                .builder()
                .leavingUser(serverUser)
                .member(event.getMember())
                .build();
    }

}
