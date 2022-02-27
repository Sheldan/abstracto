package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.GuildMessageEmbedEventModel;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageEmbedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
public class AsyncMessageEmbeddedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<AsyncMessageEmbeddedListener> listenerList;

    @Autowired
    @Qualifier("messageEmbeddedExecutor")
    private TaskExecutor messageEmbeddedListener;

    @Autowired
    private AsyncMessageEmbeddedListenerBean self;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onMessageEmbed(@NotNull MessageEmbedEvent event) {
        if(listenerList == null) return;
        GuildMessageEmbedEventModel model = getModel(event);
        listenerList.forEach(leaveListener -> listenerService.executeFeatureAwareListener(leaveListener, model, messageEmbeddedListener));
    }

    private GuildMessageEmbedEventModel getModel(MessageEmbedEvent event) {
        return GuildMessageEmbedEventModel
                .builder()
                .channelId(event.getChannel().getIdLong())
                .serverId(event.getGuild().getIdLong())
                .embeds(event.getMessageEmbeds())
                .messageId(event.getMessageIdLong())
                .build();
    }

}
