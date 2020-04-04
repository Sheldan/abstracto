package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.listener.MessageDeletedListener;
import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class MessageDeletedListenerBean extends ListenerAdapter {
    @Autowired
    private List<MessageDeletedListener> listener;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private MessageDeletedListenerBean self;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    @Transactional
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong()).thenAccept(cachedMessage -> {
            self.executeListener(cachedMessage);
        });
    }

    @Transactional
    public void executeListener(CachedMessage cachedMessage) {
        listener.forEach(messageDeletedListener -> {
            if(!featureFlagService.isFeatureEnabled(messageDeletedListener.getFeature(), cachedMessage.getServerId())) {
                return;
            }
            try {
                messageDeletedListener.execute(cachedMessage);
            } catch (AbstractoRunTimeException e) {
                log.error("Listener {} failed with exception:", messageDeletedListener.getClass().getName(), e);
            }
        });
    }
}
