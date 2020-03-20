package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.MessageTextUpdatedListener;
import dev.sheldan.abstracto.core.service.MessageCache;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;

@Component
public class MessageUpdatedListener extends ListenerAdapter {

    @Autowired
    private List<MessageTextUpdatedListener> listener;

    @Autowired
    private MessageCache messageCache;

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        Message fromCache = messageCache.getMessageFromCache(event.getMessage());
        listener.forEach(messageTextUpdatedListener -> {
            messageTextUpdatedListener.execute(fromCache, event.getMessage());
        });
        messageCache.putMessageInCache(event.getMessage());
    }
}
