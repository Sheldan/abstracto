package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.MessageTextUpdatedModel;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
@Slf4j
public class AsyncMessageUpdatedListenerBean extends ListenerAdapter {
    @Autowired(required = false)
    private List<AsyncMessageTextUpdatedListener> listener;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private AsyncMessageUpdatedListenerBean self;

    @Autowired
    @Qualifier("messageUpdatedExecutor")
    private TaskExecutor messageUpdatedExecutor;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if(listener == null) return;
        Message message = event.getMessage();
        messageCache.getMessageFromCache(message.getGuild().getIdLong(), message.getTextChannel().getIdLong(), event.getMessageIdLong())
                .thenAcceptBoth(messageCache.putMessageInCache(message), (oldCache, newCache) ->  self.executeListeners(event, oldCache))
        .exceptionally(throwable -> {
            log.error("Message retrieval {} from cache failed. ", event.getMessage().getId(), throwable);
            return null;
        });
    }

    public void executeListeners(GuildMessageUpdateEvent event, CachedMessage oldMessage) {
        MessageTextUpdatedModel model = getModel(event, oldMessage);
        listener.forEach(messageTextUpdatedListener ->
            listenerService.executeFeatureAwareListener(messageTextUpdatedListener, model, messageUpdatedExecutor)
        );
    }

    private MessageTextUpdatedModel getModel(GuildMessageUpdateEvent event, CachedMessage oldMessage) {
        return MessageTextUpdatedModel
                .builder()
                .after(event.getMessage())
                .before(oldMessage)
                .build();
    }

}
