package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ReactionRemovedListenerBean extends ListenerAdapter {

    @Autowired
    private CacheEntityService cacheEntityService;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired(required = false)
    private List<ReactionRemovedListener> reactionRemovedListeners;

    @Autowired
    private ReactionRemovedListenerBean self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private EmoteService emoteService;

    private void removeReactionIfThere(CachedMessage message, CachedReactions reaction, ServerUser userReacting) {
        Optional<CachedReactions> existingReaction = message.getReactions().stream().filter(reaction1 ->
                reaction1.getEmote().equals(reaction.getEmote())
        ).findAny();
        if(existingReaction.isPresent()) {
            CachedReactions cachedReaction = existingReaction.get();
            cachedReaction.getUsers().removeIf(user -> user.getUserId().equals(userReacting.getUserId()) && user.getServerId().equals(userReacting.getServerId()));
            message.getReactions().removeIf(reaction1 -> reaction1.getUsers().isEmpty());
        }
    }

    @Override
    @Transactional
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        if(reactionRemovedListeners == null) return;
        if(event.getUserIdLong() == botService.getInstance().getSelfUser().getIdLong()) {
            return;
        }
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage -> {
            cacheEntityService.getCachedReactionFromReaction(event.getReaction()).thenAccept(reaction ->
                self.callRemoveListeners(event, cachedMessage, reaction)
            ) .exceptionally(throwable -> {
                log.error("Failed to retrieve cached reaction for message {} ", event.getMessageIdLong(), throwable);
                return null;
            });
            messageCache.putMessageInCache(cachedMessage);
        }).exceptionally(throwable -> {
            log.error("Message retrieval {} from cache failed. ", event.getMessageIdLong(), throwable);
            return null;
        });
    }

    @Transactional
    public void callRemoveListeners(@Nonnull GuildMessageReactionRemoveEvent event, CachedMessage cachedMessage, CachedReactions reaction) {
        ServerUser serverUser = ServerUser.builder().serverId(event.getGuild().getIdLong()).userId(event.getUserIdLong()).build();
        removeReactionIfThere(cachedMessage, reaction, serverUser);
        reactionRemovedListeners.forEach(reactionRemovedListener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(reactionRemovedListener.getFeature());
            if(!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                return;
            }
            try {
                self.executeIndividualReactionRemovedListener(event, cachedMessage, serverUser, reactionRemovedListener);
            } catch (AbstractoRunTimeException e) {
                log.warn(String.format("Failed to execute reaction removed listener %s.", reactionRemovedListener.getClass().getName()), e);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualReactionRemovedListener(@Nonnull GuildMessageReactionRemoveEvent event, CachedMessage cachedMessage, ServerUser serverUser, ReactionRemovedListener reactionRemovedListener) {
        reactionRemovedListener.executeReactionRemoved(cachedMessage, event, serverUser);
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(reactionRemovedListeners);
    }

}