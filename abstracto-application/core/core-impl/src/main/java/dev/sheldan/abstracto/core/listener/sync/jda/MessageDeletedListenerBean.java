package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.ChannelNotInGuildException;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.GuildChannelMember;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListenerBean.ACTION;
import static dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListenerBean.MESSAGE_METRIC;

@Component
@Slf4j
public class MessageDeletedListenerBean extends ListenerAdapter {
    @Autowired(required = false)
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
    private GuildService guildService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MetricService metricService;

    private static final CounterMetric MESSAGE_DELETED_COUNTER =
            CounterMetric
                    .builder().name(MESSAGE_METRIC)
                    .tagList(Arrays.asList(MetricTag.getTag(ACTION, "deleted")))
                    .build();

    @Override
    @Transactional
    public void onGuildMessageDelete(@Nonnull GuildMessageDeleteEvent event) {
        metricService.incrementCounter(MESSAGE_DELETED_COUNTER);
        if(listener == null) return;
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
                .aUserInAServer(userInServerManagementService.loadOrCreateUser(cachedMessage.getServerId(), cachedMessage.getAuthor().getAuthorId()))
                .build();
        memberService.getMemberInServerAsync(cachedMessage.getServerId(), cachedMessage.getAuthor().getAuthorId()).thenAccept(member -> {
            GuildChannelMember authorMember = GuildChannelMember
                    .builder()
                    .guild(guildService.getGuildById(cachedMessage.getServerId()))
                    .textChannel(channelService.getTextChannelFromServerOptional(cachedMessage.getServerId(), cachedMessage.getChannelId()).orElseThrow(() -> new ChannelNotInGuildException(cachedMessage.getChannelId())))
                    .member(memberService.getMemberInServer(cachedMessage.getServerId(), cachedMessage.getAuthor().getAuthorId()))
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

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualMessageDeletedListener(CachedMessage cachedMessage, AServerAChannelAUser authorUser, GuildChannelMember authorMember, MessageDeletedListener messageDeletedListener) {
        log.trace("Executing message deleted listener {} for message {} in guild {}.", messageDeletedListener.getClass().getName(), cachedMessage.getMessageId(), cachedMessage.getMessageId());
        messageDeletedListener.execute(cachedMessage, authorUser, authorMember);
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_DELETED_COUNTER, "Messages deleted");
        BeanUtils.sortPrioritizedListeners(listener);
    }
}
