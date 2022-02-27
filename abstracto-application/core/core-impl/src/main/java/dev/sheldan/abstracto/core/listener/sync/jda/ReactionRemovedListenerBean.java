package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.listener.ReactionRemovedModel;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
    private ListenerService listenerService;

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
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
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
    public void callRemoveListeners(@Nonnull MessageReactionRemoveEvent event, CachedMessage cachedMessage, CachedReactions reaction) {
        ServerUser serverUser = ServerUser.builder().serverId(event.getGuild().getIdLong()).userId(event.getUserIdLong()).build();
        removeReactionIfThere(cachedMessage, reaction, serverUser);
        ReactionRemovedModel model = getModel(event, cachedMessage, serverUser);
        reactionRemovedListeners.forEach(reactionRemovedListener -> listenerService.executeFeatureAwareListener(reactionRemovedListener, model));
    }

    private ReactionRemovedModel getModel(MessageReactionRemoveEvent event, CachedMessage cachedMessage, ServerUser userRemoving) {
        return ReactionRemovedModel
                .builder()
                .memberRemoving(event.getMember())
                .reaction(event.getReaction())
                .userRemoving(userRemoving)
                .message(cachedMessage)
                .build();
    }


    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(reactionRemovedListeners);
    }

}
