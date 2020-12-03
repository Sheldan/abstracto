package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.listener.GuildMessageEmbedEventModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
public class MessageEmbeddedListenerBean extends ListenerAdapter {

    @Autowired
    private MessageCache messageCache;

    @Autowired
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
    private MessageEmbeddedListenerBean self;

    @Override
    public void onGuildMessageEmbed(@NotNull GuildMessageEmbedEvent event) {
        GuildMessageEmbedEventModel model = buildModel(event);
        listenerList.forEach(messageReceivedListener -> {
            try {
                FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageReceivedListener.getFeature());
                if(!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                    return;
                }
                self.executeIndividualGuildMessageReceivedListener(model, messageReceivedListener);
            } catch (Exception e) {
                log.error("Listener {} had exception when executing.", messageReceivedListener, e);
               // exceptionService.reportExceptionToGuildMessageReceivedContext(e, event);
            }
        });
    }

    private GuildMessageEmbedEventModel buildModel(GuildMessageEmbedEvent event) {
        return GuildMessageEmbedEventModel
                .builder()
                .channel(event.getChannel())
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
        listenerList.sort(Comparator.comparing(Prioritized::getPriority).reversed());
    }
}
