package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.BoostTimeUpdatedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class AsyncMemberBoostTimeUpdateListenerBean extends ListenerAdapter {
    @Autowired(required = false)
    private List<AsyncMemberBoostTimeUpdateListener> listenerList;

    @Autowired
    @Qualifier("boostTimeUpdateExecutor")
    private TaskExecutor boostTimeUpdateListener;

    @Autowired
    private ListenerService listenerService;

    @Override
    public void onGuildMemberUpdateBoostTime(@Nonnull GuildMemberUpdateBoostTimeEvent event) {
        if(listenerList == null) return;
        BoostTimeUpdatedModel model = getModel(event);
        listenerList.forEach(boostListener -> listenerService.executeFeatureAwareListener(boostListener, model, boostTimeUpdateListener));
    }

    private BoostTimeUpdatedModel getModel(GuildMemberUpdateBoostTimeEvent event) {
        return BoostTimeUpdatedModel
                .builder()
                .member(event.getMember())
                .oldTime(event.getOldTimeBoosted())
                .newTime(event.getNewTimeBoosted())
                .build();
    }
}
