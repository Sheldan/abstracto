package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.MessageTextUpdatedListener;
import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class MessageUpdatedListener extends ListenerAdapter {

    @Autowired
    private List<MessageTextUpdatedListener> listener;

    @Autowired
    private MessageCache messageCache;

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        Message message = event.getMessage();
        try {
            CachedMessage fromCache = messageCache.getMessageFromCache(message.getGuild().getIdLong(), message.getTextChannel().getIdLong(), event.getMessageIdLong());
            listener.forEach(messageTextUpdatedListener -> {
                messageTextUpdatedListener.execute(fromCache, message);
            });
            messageCache.putMessageInCache(message);
        } catch (ExecutionException | InterruptedException e) {
            log.warn("Failed to load message", e);
        }
    }
}
