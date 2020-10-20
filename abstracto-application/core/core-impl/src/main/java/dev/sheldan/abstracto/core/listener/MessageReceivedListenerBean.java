package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
public class MessageReceivedListenerBean extends ListenerAdapter {

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private List<MessageReceivedListener> listenerList;

    @Autowired
    private List<PrivateMessageReceivedListener> privateMessageReceivedListeners;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired
    private MessageReceivedListenerBean self;

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        messageCache.putMessageInCache(event.getMessage());
        listenerList.forEach(messageReceivedListener -> {
            try {
                FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageReceivedListener.getFeature());
                if(!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                    return;
                }
                self.executeIndividualGuildMessageReceivedListener(event, messageReceivedListener);
            } catch (Exception e) {
                log.error("Listener {} had exception when executing.", messageReceivedListener, e);
                exceptionService.reportExceptionToGuildMessageReceivedContext(e, event);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeIndividualGuildMessageReceivedListener(@Nonnull GuildMessageReceivedEvent event, MessageReceivedListener messageReceivedListener) {
        messageReceivedListener.execute(event.getMessage());
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        if(event.getAuthor().getId().equals(botService.getInstance().getSelfUser().getId())) {
            return;
        }
        privateMessageReceivedListeners.forEach(messageReceivedListener -> {
            try {
                self.executeIndividualPrivateMessageReceivedListener(event, messageReceivedListener);
            } catch (Exception e) {
                log.error("Listener {} had exception when executing.", messageReceivedListener, e);
                exceptionService.reportExceptionToPrivateMessageReceivedContext(e, event);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeIndividualPrivateMessageReceivedListener(@Nonnull PrivateMessageReceivedEvent event, PrivateMessageReceivedListener messageReceivedListener) {
        log.trace("Executing private message listener {} for member {}.", messageReceivedListener.getClass().getName(), event.getAuthor().getId());
        messageReceivedListener.execute(event.getMessage());
    }

    @PostConstruct
    public void postConstruct() {
        listenerList.sort(Comparator.comparing(Prioritized::getPriority).reversed());
        privateMessageReceivedListeners.sort(Comparator.comparing(Prioritized::getPriority).reversed());
    }
}
