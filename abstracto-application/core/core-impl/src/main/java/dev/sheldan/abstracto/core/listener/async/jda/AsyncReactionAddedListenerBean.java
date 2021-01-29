package dev.sheldan.abstracto.core.listener.async.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AsyncReactionAddedListenerBean extends ListenerAdapter {

    @Autowired
    private CacheEntityService cacheEntityService;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired(required = false)
    private List<AsyncReactionAddedListener> addedListenerList;

    @Autowired
    private AsyncReactionAddedListenerBean self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    @Qualifier("reactionAddedExecutor")
    private TaskExecutor reactionAddedTaskExecutor;

    @Override
    @Transactional
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if(addedListenerList == null) return;
        if(event.getUserIdLong() == botService.getInstance().getSelfUser().getIdLong()) {
            return;
        }
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage ->
                cacheEntityService.getCachedReactionFromReaction(event.getReaction()).thenAccept(reaction ->
                self.callAddedListeners(event, cachedMessage, reaction)
            ).exceptionally(throwable -> {
                log.error("Failed to handle add reaction to message {} ", event.getMessageIdLong(), throwable);
                return null;
            })
        ).exceptionally(throwable -> {
            log.error("Message retrieval {} from cache failed. ", event.getMessageIdLong(), throwable);
            return null;
        });
    }

    private void addReactionIfNotThere(CachedMessage message, CachedReactions reaction, ServerUser userReacting) {
        Optional<CachedReactions> existingReaction = message.getReactions().stream().filter(reaction1 ->
                reaction1.getEmote().equals(reaction.getEmote())
        ).findAny();
        if(!existingReaction.isPresent()) {
            message.getReactions().add(reaction);
        } else {
            CachedReactions cachedReaction = existingReaction.get();
            Optional<ServerUser> any = cachedReaction.getUsers().stream().filter(user -> user.getUserId().equals(userReacting.getUserId()) && user.getServerId().equals(userReacting.getServerId())).findAny();
            if(!any.isPresent()){
                cachedReaction.getUsers().add(userReacting);
            }
        }
    }

    @Transactional
    public void callAddedListeners(@Nonnull GuildMessageReactionAddEvent event, CachedMessage cachedMessage, CachedReactions reaction) {
        ServerUser serverUser = ServerUser.builder().serverId(event.getGuild().getIdLong()).userId(event.getUserIdLong()).build();
        addReactionIfNotThere(cachedMessage, reaction, serverUser);
        messageCache.putMessageInCache(cachedMessage);
        addedListenerList.forEach(reactedAddedListener ->
            CompletableFuture.runAsync(() ->
                self.executeIndividualReactionAddedListener(reaction, cachedMessage, serverUser, reactedAddedListener)
            , reactionAddedTaskExecutor)
            .exceptionally(throwable -> {
                log.error("Async reaction added listener {} failed with exception.", reactedAddedListener, throwable);
                return null;
            })
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualReactionAddedListener(@Nonnull CachedReactions reaction, CachedMessage cachedMessage, ServerUser serverUser, AsyncReactionAddedListener reactedAddedListener) {
        FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(reactedAddedListener.getFeature());
        if(!featureFlagService.isFeatureEnabled(feature, serverUser.getServerId())) {
            return;
        }
        try {
            reactedAddedListener.executeReactionAdded(cachedMessage, reaction, serverUser);
        } catch (Exception e) {
            log.warn(String.format("Failed to execute reaction added listener %s.", reactedAddedListener.getClass().getName()), e);
        }
    }
}
