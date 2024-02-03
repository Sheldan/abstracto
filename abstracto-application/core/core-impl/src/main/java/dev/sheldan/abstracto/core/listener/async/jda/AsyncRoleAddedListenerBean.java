package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.listener.sync.jda.RoleAddedListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.RoleAddedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
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
public class AsyncRoleAddedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<RoleAddedListener> listenerList;

    @Autowired
    private AsyncRoleAddedListenerBean self;

    @Autowired
    @Qualifier("reactionAddedExecutor")
    private TaskExecutor reactionAddedTaskExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        if(listenerList == null) return;
        if(event.getUser().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            return;
        }
        self.callAddedListeners(event, event.getMember());
    }

    @Transactional
    public void callAddedListeners(GuildMemberRoleAddEvent event, Member member) {
        ServerUser serverUser = ServerUser
                .builder()
                .serverId(event.getGuild().getIdLong())
                .userId(event.getUser().getIdLong())
                .isBot(event.getUser().isBot())
                .build();
        event.getRoles().forEach(role -> {
            RoleAddedModel model = getModel(role, serverUser, member);
            listenerList.forEach(asyncReactionAddedListener -> listenerService.executeFeatureAwareListener(asyncReactionAddedListener, model, reactionAddedTaskExecutor));
        });
    }

    private RoleAddedModel getModel(Role role, ServerUser targetUser, Member member) {
        return RoleAddedModel
                .builder()
                .role(role)
                .roleId(role.getIdLong())
                .targetMember(member)
                .targetUser(targetUser)
                .build();
    }
}
