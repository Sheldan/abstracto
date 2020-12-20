package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.execution.result.MessageReceivedListenerResult;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
public class MessageReceivedListenerBean extends ListenerAdapter {

    @Autowired
    private MessageCache messageCache;

    @Autowired(required = false)
    private List<MessageReceivedListener> listenerList;

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
        if(listenerList == null) return;
        for (MessageReceivedListener messageReceivedListener : listenerList) {
            try {
                FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageReceivedListener.getFeature());
                if (!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                    continue;
                }
                MessageReceivedListenerResult result = self.executeIndividualGuildMessageReceivedListener(event, messageReceivedListener);
                if (messageReceivedListener.shouldConsume(event, result)) {
                    break;
                }
            } catch (Exception e) {
                log.error("Listener {} had exception when executing.", messageReceivedListener, e);
                exceptionService.reportExceptionToGuildMessageReceivedContext(e, event);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MessageReceivedListenerResult executeIndividualGuildMessageReceivedListener(@Nonnull GuildMessageReceivedEvent event, MessageReceivedListener messageReceivedListener) {
        return messageReceivedListener.execute(event.getMessage());
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
