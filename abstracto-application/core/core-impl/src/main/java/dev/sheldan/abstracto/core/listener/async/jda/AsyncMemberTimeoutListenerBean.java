package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberTimeoutUpdatedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateTimeOutEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

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

    @Override
    public void onGuildMemberUpdateTimeOut(@Nonnull GuildMemberUpdateTimeOutEvent event) {
        if(listenerList == null) return;
        MemberTimeoutUpdatedModel model = getModel(event);
        listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, memberTimeoutExecutor));
    }

    private MemberTimeoutUpdatedModel getModel(GuildMemberUpdateTimeOutEvent event) {
        return MemberTimeoutUpdatedModel
                .builder()
                .oldTimeout(event.getOldTimeOutEnd())
                .newTimeout(event.getNewTimeOutEnd())
                .member(event.getMember())
                .event(event)
                .timeoutUser(ServerUser.fromMember(event.getMember()))
                .guild(event.getGuild())
                .user(event.getUser())
                .build();
    }
}
