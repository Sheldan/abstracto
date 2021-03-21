package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberJoinModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
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
public class AsyncJoinListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncJoinListener> listenerList;

    @Autowired
    @Qualifier("joinListenerExecutor")
    private TaskExecutor joinListenerExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        if(listenerList == null) return;
        MemberJoinModel model = getModel(event);
        listenerList.forEach(joinListener -> listenerService.executeFeatureAwareListener(joinListener, model, joinListenerExecutor));
    }

    private MemberJoinModel getModel(GuildMemberJoinEvent event) {
        ServerUser serverUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getUser().getIdLong())
                .build();
        return MemberJoinModel
                .builder()
                .joiningUser(serverUser)
                .member(event.getMember())
                .build();
    }
}
