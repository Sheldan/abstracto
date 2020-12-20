package dev.sheldan.abstracto.core.listener.async.jda;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AsyncMessageEmbeddedListenerBean extends ListenerAdapter {

    @Autowired
    private MessageCache messageCache;

    @Autowired(required = false)
    private List<AsyncMessageEmbeddedListener> listenerList;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired
    @Qualifier("messageEmbeddedExecutor")
    private TaskExecutor messageEmbeddedListener;

    @Autowired
    private AsyncMessageEmbeddedListenerBean self;

    @Override
    public void onGuildMessageEmbed(@NotNull GuildMessageEmbedEvent event) {
        if(listenerList == null) return;
        GuildMessageEmbedEventModel model = buildModel(event);
        listenerList.forEach(messageReceivedListener ->
            CompletableFuture.runAsync(() ->
                self.executeIndividualGuildMessageReceivedListener(model, messageReceivedListener)
            , messageEmbeddedListener).exceptionally(throwable -> {
                log.error("Async message embedded listener {} failed with exception.", messageReceivedListener, throwable);
                return null;
            })
        );
    }

    private GuildMessageEmbedEventModel buildModel(GuildMessageEmbedEvent event) {
        return GuildMessageEmbedEventModel
                .builder()
                .channelId(event.getChannel().getIdLong())
                .serverId(event.getGuild().getIdLong())
                .embeds(event.getMessageEmbeds())
                .messageId(event.getMessageIdLong())
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualGuildMessageReceivedListener(GuildMessageEmbedEventModel model, AsyncMessageEmbeddedListener messageReceivedListener) {
        try {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageReceivedListener.getFeature());
            if(!featureFlagService.isFeatureEnabled(feature, model.getServerId())) {
                return;
            }
            messageReceivedListener.execute(model);
        } catch (Exception e) {
            log.error("Listener {} had exception when executing.", messageReceivedListener, e);
        }
    }

}
