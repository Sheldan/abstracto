package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.ChannelNotFoundException;
import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

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
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private BotService botService;

    @Override
    @Transactional
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        Consumer<CachedMessage> cachedMessageConsumer = cachedMessage -> self.executeListener(cachedMessage);
        messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong())
                .thenAccept(cachedMessageConsumer)
                .exceptionally(throwable -> {
                    log.error("Message retrieval {} from cache failed. ", event.getMessageIdLong(), throwable);
                    return null;
                });
    }

    @Transactional
    public void executeListener(CachedMessage cachedMessage) {
        // TODO maybe lazy load, when there is actually a listener which has its feature enabled
        AServerAChannelAUser authorUser = AServerAChannelAUser
                .builder()
                .guild(serverManagementService.loadOrCreate(cachedMessage.getServerId()))
                .channel(channelManagementService.loadChannel(cachedMessage.getChannelId()))
                .aUserInAServer(userInServerManagementService.loadUser(cachedMessage.getServerId(), cachedMessage.getAuthorId()))
                .build();
        botService.getMemberInServerAsync(cachedMessage.getServerId(), cachedMessage.getAuthorId()).thenAccept(member -> {
            GuildChannelMember authorMember = GuildChannelMember
                    .builder()
                    .guild(botService.getGuildById(cachedMessage.getServerId()))
                    .textChannel(botService.getTextChannelFromServerOptional(cachedMessage.getServerId(), cachedMessage.getChannelId()).orElseThrow(() -> new ChannelNotFoundException(cachedMessage.getChannelId())))
                    .member(botService.getMemberInServer(cachedMessage.getServerId(), cachedMessage.getAuthorId()))
                    .build();
            listener.forEach(messageDeletedListener -> {
                FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(messageDeletedListener.getFeature());
                if(!featureFlagService.isFeatureEnabled(feature, cachedMessage.getServerId())) {
                    return;
                }
                try {
                    self.executeIndividualMessageDeletedListener(cachedMessage, authorUser, authorMember, messageDeletedListener);
                } catch (AbstractoRunTimeException e) {
                    log.error("Listener {} failed with exception:", messageDeletedListener.getClass().getName(), e);
                }
            });
        }).exceptionally(throwable -> {
            log.error("Message deleted listener failed.", throwable);
            return null;
        });

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void executeIndividualMessageDeletedListener(CachedMessage cachedMessage, AServerAChannelAUser authorUser, GuildChannelMember authorMember, MessageDeletedListener messageDeletedListener) {
        log.trace("Executing message deleted listener {} for message {} in guild {}.", messageDeletedListener.getClass().getName(), cachedMessage.getMessageId(), cachedMessage.getMessageId());
        messageDeletedListener.execute(cachedMessage, authorUser, authorMember);
    }

    @PostConstruct
    public void postConstruct() {
        listener.sort(Comparator.comparing(Prioritized::getPriority).reversed());
    }
}
