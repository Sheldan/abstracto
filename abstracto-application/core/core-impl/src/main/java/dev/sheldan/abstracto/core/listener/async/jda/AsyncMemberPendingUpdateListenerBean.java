package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MemberUpdatePendingModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
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
public class AsyncMemberPendingUpdateListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncUpdatePendingListener> listenerList;

    @Autowired
    @Qualifier("memberPendingUpdateExecutor")
    private TaskExecutor memberPendingUpdateListenerExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onGuildMemberUpdatePending(@Nonnull GuildMemberUpdatePendingEvent event) {
        if(listenerList == null) return;
        MemberUpdatePendingModel model = getModel(event);
        listenerList.forEach(joinListener -> listenerService.executeFeatureAwareListener(joinListener, model, memberPendingUpdateListenerExecutor));
    }

    private MemberUpdatePendingModel getModel(GuildMemberUpdatePendingEvent event) {
        ServerUser serverUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getUser().getIdLong())
                .isBot(event.getUser().isBot())
                .build();
        return MemberUpdatePendingModel
                .builder()
                .user(serverUser)
                .member(event.getMember())
                .build();
    }
}
