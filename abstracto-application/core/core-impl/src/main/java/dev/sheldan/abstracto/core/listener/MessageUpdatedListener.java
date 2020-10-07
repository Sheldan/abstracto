package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
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

    @Autowired
    private FeatureConfigService featureConfigService;

    @Override
    public void onGuildMessageUpdate(@Nonnull GuildMessageUpdateEvent event) {
        Message message = event.getMessage();
        messageCache.getMessageFromCache(message.getGuild().getIdLong(), message.getTextChannel().getIdLong(), event.getMessageIdLong()).thenAccept(cachedMessage -> {
            self.executeListener(message, cachedMessage);
            messageCache.putMessageInCache(message);
        }).exceptionally(throwable -> {
            log.error("Message retrieval {} from cache failed. ", event.getMessage().getId(), throwable);
            return null;
        });

    }

    @Transactional
    public void executeListener(Message message, CachedMessage cachedMessage) {
        listener.forEach(messageTextUpdatedListener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageTextUpdatedListener.getFeature());
            if(!featureFlagService.isFeatureEnabled(feature, message.getGuild().getIdLong())) {
                return;
            }
            try {
                self.executeIndividualMessageUpdatedListener(message, cachedMessage, messageTextUpdatedListener);
            } catch (AbstractoRunTimeException e) {
                log.error(String.format("Failed to execute listener. %s", messageTextUpdatedListener.getClass().getName()), e);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeIndividualMessageUpdatedListener(Message message, CachedMessage cachedMessage, MessageTextUpdatedListener messageTextUpdatedListener) {
        messageTextUpdatedListener.execute(cachedMessage, message);
    }
}
