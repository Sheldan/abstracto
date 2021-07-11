package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.VoiceChannelJoinedModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AsyncVoiceChannelJoinedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncVoiceChannelJoinedListener> listenerList;

    @Autowired
    @Qualifier("voiceChatJoinedExecutor")
    private TaskExecutor voiceChatJoinedExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if(listenerList == null) return;
        VoiceChannelJoinedModel model = getModel(event);
        listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, voiceChatJoinedExecutor));
    }

    private VoiceChannelJoinedModel getModel(GuildVoiceJoinEvent event) {
        return VoiceChannelJoinedModel
                .builder()
                .channel(event.getChannelJoined())
                .member(event.getMember())
                .build();
    }
}
