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
    private List<PrivateMessageReceivedListener> privateMessageReceivedListeners;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private ExceptionService exceptionService;

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        messageCache.putMessageInCache(event.getMessage());
        listenerList.forEach(messageReceivedListener -> {
            try {
                FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageReceivedListener.getFeature());
                if(!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                    return;
                }
                messageReceivedListener.execute(event.getMessage());
            } catch (Exception e) {
                log.error("Listener {} had exception when executing.", messageReceivedListener, e);
                exceptionService.reportExceptionToGuildMessageReceivedContext(e, event);
            }
        });
    }

    @Override
    public void onPrivateMessageReceived(@Nonnull PrivateMessageReceivedEvent event) {
        if(event.getAuthor().getId().equals(botService.getInstance().getSelfUser().getId())) {
            return;
        }
        privateMessageReceivedListeners.forEach(messageReceivedListener -> {
            try {
                messageReceivedListener.execute(event.getMessage());
            } catch (Exception e) {
                log.error("Listener {} had exception when executing.", messageReceivedListener, e);
                exceptionService.reportExceptionToPrivateMessageReceivedContext(e, event);
            }
        });
    }
}
