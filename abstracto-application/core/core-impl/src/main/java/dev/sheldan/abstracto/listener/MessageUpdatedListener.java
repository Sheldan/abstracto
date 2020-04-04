package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.listener.MessageTextUpdatedListener;
import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class MessageUpdatedListener extends ListenerAdapter {

    @Autowired
    private List<MessageTextUpdatedListener> listener;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private MessageUpdatedListener self;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        Message message = event.getMessage();
        messageCache.getMessageFromCache(message.getGuild().getIdLong(), message.getTextChannel().getIdLong(), event.getMessageIdLong()).thenAccept(cachedMessage -> {
            self.executeListener(message, cachedMessage);
            messageCache.putMessageInCache(message);
        });

    }

    @Transactional
    public void executeListener(Message message, CachedMessage cachedMessage) {
        listener.forEach(messageTextUpdatedListener -> {
            if(!featureFlagService.isFeatureEnabled(messageTextUpdatedListener.getFeature(), message.getGuild().getIdLong())) {
                return;
            }
            try {
                messageTextUpdatedListener.execute(cachedMessage, message);
            } catch (AbstractoRunTimeException e) {
                log.error(String.format("Failed to execute listener. %s", messageTextUpdatedListener.getClass().getName()), e);
            }
        });
    }
}
