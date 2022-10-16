package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.VoiceChannelLeftModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class AsyncVoiceChannelLeftListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncVoiceChannelLeftListener> listenerList;

    @Autowired
    @Qualifier("voiceChatLeftExecutor")
    private TaskExecutor voiceChatLeaveExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    public void onGuildVoiceUpdate(@Nonnull GuildVoiceUpdateEvent event) {
        if(listenerList == null) return;
        if(event.getChannelJoined() == null && event.getChannelLeft() != null) {
            VoiceChannelLeftModel model = getModel(event);
            listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, voiceChatLeaveExecutor));
        }
    }

    private VoiceChannelLeftModel getModel(GuildVoiceUpdateEvent event) {
        return VoiceChannelLeftModel
                .builder()
                .channel(event.getChannelLeft())
                .member(event.getMember())
                .build();
    }
}
