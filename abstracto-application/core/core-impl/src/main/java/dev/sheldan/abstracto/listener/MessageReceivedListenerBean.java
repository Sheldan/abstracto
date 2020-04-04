package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.listener.MessageReceivedListener;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

@Component
@Slf4j
public class MessageReceivedListenerBean extends ListenerAdapter {

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private List<MessageReceivedListener> listenerList;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Override
    @Transactional
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        messageCache.putMessageInCache(event.getMessage());
        listenerList.forEach(messageReceivedListener -> {
            if(!featureFlagService.isFeatureEnabled(messageReceivedListener.getFeature(), event.getGuild().getIdLong())) {
                return;
            }
            messageReceivedListener.execute(event.getMessage());
        });
    }

}
