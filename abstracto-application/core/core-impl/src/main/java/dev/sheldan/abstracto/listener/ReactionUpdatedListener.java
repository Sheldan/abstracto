package dev.sheldan.abstracto.listener;

import dev.sheldan.abstracto.core.listener.ReactedAddedListener;
import dev.sheldan.abstracto.core.listener.ReactedRemovedListener;
import dev.sheldan.abstracto.core.management.UserManagementService;
import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.CachedReaction;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.utils.EmoteUtils;
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
    private EmoteService emoteService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private List<ReactedAddedListener> addedListenerList;

    @Autowired
    private List<ReactedRemovedListener> reactionRemovedListener;

    @Autowired
    private ReactionUpdatedListener self;


    @Override
    @Transactional
    public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage -> {
            CompletableFuture<CachedReaction> future = new CompletableFuture<>();
            messageCache.getCachedReactionFromReaction(future, event.getReaction());
            future.thenAccept(reaction -> {
                self.callAddedListeners(event, cachedMessage, reaction);
                messageCache.putMessageInCache(cachedMessage);
            });
        });
    }

    private void addReactionIfNotThere(CachedMessage message, CachedReaction reaction, AUser userReacting) {
        Optional<CachedReaction> existingReaction = message.getReactions().stream().filter(reaction1 -> {
            return EmoteUtils.compareAEmote(reaction1.getEmote(), reaction.getEmote());
        }).findAny();
        if(!existingReaction.isPresent()) {
            message.getReactions().add(reaction);
        } else {
            CachedReaction cachedReaction = existingReaction.get();
            Optional<AUser> any = cachedReaction.getUsers().stream().filter(user -> user.getId().equals(userReacting.getId())).findAny();
            if(!any.isPresent()){
                cachedReaction.getUsers().add(userReacting);
            }
        }
    }

    private void removeReactionIfThere(CachedMessage message, CachedReaction reaction, AUser userReacting) {
        Optional<CachedReaction> existingReaction = message.getReactions().stream().filter(reaction1 -> {
            return EmoteUtils.compareAEmote(reaction1.getEmote(), reaction.getEmote());
        }).findAny();
        if(existingReaction.isPresent()) {
            CachedReaction cachedReaction = existingReaction.get();
            cachedReaction.getUsers().removeIf(user -> user.getId().equals(userReacting.getId()));
            message.getReactions().removeIf(reaction1 -> reaction1.getUsers().isEmpty());
        }
    }

    @Transactional
    public void callAddedListeners(@Nonnull GuildMessageReactionAddEvent event, CachedMessage cachedMessage, CachedReaction reaction) {
        AUserInAServer userInAServer = userManagementService.loadUser(event.getGuild().getIdLong(), event.getUserIdLong());
        addReactionIfNotThere(cachedMessage, reaction, userInAServer.getUserReference());
        try {
            addedListenerList.forEach(reactedAddedListener -> {
                reactedAddedListener.executeReactionAdded(cachedMessage, event.getReaction(), userInAServer);
            });
        } catch (Exception e) {
            log.warn("Exception on reaction added handler:", e);
        }
    }

    @Override
    @Transactional
    public void onGuildMessageReactionRemove(@Nonnull GuildMessageReactionRemoveEvent event) {
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage -> {
            CompletableFuture<CachedReaction> future = new CompletableFuture<>();
            messageCache.getCachedReactionFromReaction(future, event.getReaction());
            future.thenAccept(reaction -> {
                self.callRemoveListeners(event, cachedMessage, reaction);
            });

            messageCache.putMessageInCache(cachedMessage);
        });
    }

    @Transactional
    public void callRemoveListeners(@Nonnull GuildMessageReactionRemoveEvent event, CachedMessage cachedMessage, CachedReaction reaction) {
        AUserInAServer userInAServer = userManagementService.loadUser(event.getGuild().getIdLong(), event.getUserIdLong());
        removeReactionIfThere(cachedMessage, reaction, userInAServer.getUserReference());
        reactionRemovedListener.forEach(reactedAddedListener -> {
            reactedAddedListener.executeReactionRemoved(cachedMessage, event.getReaction(), userInAServer);
        });
    }

    @Override
    @Transactional
    public void onGuildMessageReactionRemoveAll(@Nonnull GuildMessageReactionRemoveAllEvent event) {
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        asyncMessageFromCache.thenAccept(cachedMessage -> {
            cachedMessage.getReactions().clear();
            messageCache.putMessageInCache(cachedMessage);
        });
    }

}
