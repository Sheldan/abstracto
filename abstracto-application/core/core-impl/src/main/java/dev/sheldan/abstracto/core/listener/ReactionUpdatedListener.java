package dev.sheldan.abstracto.core.listener;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ReactionUpdatedListener extends ListenerAdapter {

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private List<ReactedAddedListener> addedListenerList;

    @Autowired
    private List<ReactionClearedListener> clearedListenerList;

    @Autowired
    private List<ReactedRemovedListener> reactionRemovedListeners;

    @Autowired
    private ReactionUpdatedListener self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private EmoteService emoteService;

    @Override
    @Transactional
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        if(event.getUserIdLong() == botService.getInstance().getSelfUser().getIdLong()) {
            return;
        }
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage ->
            messageCache.getCachedReactionFromReaction(event.getReaction()).thenAccept(reaction -> {
                self.callAddedListeners(event, cachedMessage, reaction);
                messageCache.putMessageInCache(cachedMessage);
            }).exceptionally(throwable -> {
                log.error("Failed to add reaction to message {} ", event.getMessageIdLong(), throwable);
                return null;
            })
        ).exceptionally(throwable -> {
            log.error("Message retrieval {} from cache failed. ", event.getMessageIdLong(), throwable);
            return null;
        });
    }

    private void addReactionIfNotThere(CachedMessage message, CachedReaction reaction, AUserInAServer userReacting) {
        Optional<CachedReaction> existingReaction = message.getReactions().stream().filter(reaction1 ->
                emoteService.compareAEmote(reaction1.getEmote(), reaction.getEmote())
        ).findAny();
        if(!existingReaction.isPresent()) {
            message.getReactions().add(reaction);
        } else {
            CachedReaction cachedReaction = existingReaction.get();
            Optional<Long> any = cachedReaction.getUserInServersIds().stream().filter(user -> user.equals(userReacting.getUserInServerId())).findAny();
            if(!any.isPresent()){
                cachedReaction.getUserInServersIds().add(userReacting.getUserInServerId());
            }
        }
    }

    private void removeReactionIfThere(CachedMessage message, CachedReaction reaction, AUserInAServer userReacting) {
        Optional<CachedReaction> existingReaction = message.getReactions().stream().filter(reaction1 ->
            emoteService.compareAEmote(reaction1.getEmote(), reaction.getEmote())
        ).findAny();
        if(existingReaction.isPresent()) {
            CachedReaction cachedReaction = existingReaction.get();
            cachedReaction.getUserInServersIds().removeIf(user -> user.equals(userReacting.getUserInServerId()));
            message.getReactions().removeIf(reaction1 -> reaction1.getUserInServersIds().isEmpty());
        }
    }

    @Transactional
    public void callAddedListeners(@Nonnull GuildMessageReactionAddEvent event, CachedMessage cachedMessage, CachedReaction reaction) {
        AUserInAServer userInAServer = userInServerManagementService.loadUser(event.getGuild().getIdLong(), event.getUserIdLong());
        addReactionIfNotThere(cachedMessage, reaction, userInAServer);
        addedListenerList.forEach(reactedAddedListener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(reactedAddedListener.getFeature());
            if(!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                return;
            }
            try {
                reactedAddedListener.executeReactionAdded(cachedMessage, event.getReaction(), userInAServer);
            } catch (Exception e) {
                log.warn(String.format("Failed to execute reaction added listener %s.", reactedAddedListener.getClass().getName()), e);
            }
        });
    }

    @Override
    @Transactional
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        if(event.getUserIdLong() == botService.getInstance().getSelfUser().getIdLong()) {
            return;
        }
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage -> {
            messageCache.getCachedReactionFromReaction(event.getReaction()).thenAccept(reaction ->
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
    public void callRemoveListeners(@Nonnull GuildMessageReactionRemoveEvent event, CachedMessage cachedMessage, CachedReaction reaction) {
        AUserInAServer userInAServer = userInServerManagementService.loadUser(event.getGuild().getIdLong(), event.getUserIdLong());
        removeReactionIfThere(cachedMessage, reaction, userInAServer);
        reactionRemovedListeners.forEach(reactionRemovedListener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(reactionRemovedListener.getFeature());
            if(!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                return;
            }
            try {
                reactionRemovedListener.executeReactionRemoved(cachedMessage, event.getReaction(), userInAServer);
            } catch (AbstractoRunTimeException e) {
                log.warn(String.format("Failed to execute reaction removed listener %s.", reactionRemovedListener.getClass().getName()), e);
            }
        });
    }

    @Transactional
    public void callClearListeners(@Nonnull GuildMessageReactionRemoveAllEvent event, CachedMessage cachedMessage) {
        clearedListenerList.forEach(reactionRemovedListener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(reactionRemovedListener.getFeature());
            if(!featureFlagService.isFeatureEnabled(feature, event.getGuild().getIdLong())) {
                return;
            }
            try {
                reactionRemovedListener.executeReactionCleared(cachedMessage);
            } catch (AbstractoRunTimeException e) {
                log.warn(String.format("Failed to execute reaction clear listener %s.", reactionRemovedListener.getClass().getName()), e);
            }
        });
    }

    @Override
    @Transactional
    public void onGuildMessageReactionRemoveAll(@Nonnull GuildMessageReactionRemoveAllEvent event) {
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage -> {
            cachedMessage.getReactions().clear();
            messageCache.putMessageInCache(cachedMessage);
            self.callClearListeners(event, cachedMessage);
        }) .exceptionally(throwable -> {
            log.error("Message retrieval from cache failed for message {}", event.getMessageIdLong(), throwable);
            return null;
        });
    }

}
