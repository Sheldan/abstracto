package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.listener.GuildMessageEmbedEventModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageEmbedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
public class MessageEmbeddedListenerBean extends ListenerAdapter {

    @Autowired
    private MessageCache messageCache;

    @Autowired(required = false)
    private List<MessageEmbeddedListener> listenerList;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onMessageEmbed(@NotNull MessageEmbedEvent event) {
        if(listenerList == null) return;
        GuildMessageEmbedEventModel model = buildModel(event);
        listenerList.forEach(messageReceivedListener -> listenerService.executeFeatureAwareListener(messageReceivedListener, model));
    }

    private GuildMessageEmbedEventModel buildModel(MessageEmbedEvent event) {
        return GuildMessageEmbedEventModel
                .builder()
                .channelId(event.getChannel().getIdLong())
                .serverId(event.getGuild().getIdLong())
                .embeds(event.getMessageEmbeds())
                .messageId(event.getMessageIdLong())
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualGuildMessageReceivedListener(GuildMessageEmbedEventModel model, MessageEmbeddedListener messageReceivedListener) {
        messageReceivedListener.execute(model);
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
