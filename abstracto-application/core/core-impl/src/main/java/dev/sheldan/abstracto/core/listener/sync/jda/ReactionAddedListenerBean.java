package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.listener.ListenerService;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.listener.ReactionAddedModel;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ReactionAddedListenerBean extends ListenerAdapter {

    @Autowired
    private CacheEntityService cacheEntityService;

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired(required = false)
    private List<ReactionAddedListener> addedListenerList;

    @Autowired
    private ReactionAddedListenerBean self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private BotService botService;

    @Autowired
    private ListenerService listenerService;

    @Override
    @Transactional
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if(addedListenerList == null) return;
        if(event.getUserIdLong() == botService.getInstance().getSelfUser().getIdLong()) {
            return;
        }
        CompletableFuture<Member> memberFuture = event.retrieveMember().submit();
        CompletableFuture<CachedMessage> asyncMessageFromCache = messageCache.getMessageFromCache(event.getGuild().getIdLong(), event.getChannel().getIdLong(), event.getMessageIdLong());
        CompletableFuture<CachedReactions> reactionCacheFuture = cacheEntityService.getCachedReactionFromReaction(event.getReaction());
        FutureUtils.toSingleFuture(Arrays.asList(asyncMessageFromCache, memberFuture, reactionCacheFuture)).thenAccept(aVoid -> {
            CachedMessage cachedMessage = asyncMessageFromCache.join();
            CachedReactions reaction = reactionCacheFuture.join();
            Member member = memberFuture.join();
            self.callAddedListeners(event, cachedMessage, reaction, member);
            messageCache.putMessageInCache(cachedMessage);
        }).exceptionally(throwable -> {
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
            Optional<ServerUser> any = cachedReaction.getUsers().stream().filter(user -> user.getServerId().equals(userReacting.getServerId()) && user.getUserId().equals(userReacting.getUserId())).findAny();
            if(!any.isPresent()){
                cachedReaction.getUsers().add(userReacting);
            }
        }
    }

    @Transactional
    public void callAddedListeners(@Nonnull MessageReactionAddEvent event, CachedMessage cachedMessage, CachedReactions reaction, Member member) {
        ServerUser serverUser = ServerUser
                .builder()
                .serverId(cachedMessage.getServerId())
                .userId(event.getUserIdLong())
                .isBot(event.getUser() != null ? event.getUser().isBot() : null)
                .build();
        addReactionIfNotThere(cachedMessage, reaction, serverUser);
        ReactionAddedModel model = getModel(event, cachedMessage, serverUser, member);
        addedListenerList.forEach(reactedAddedListener -> listenerService.executeFeatureAwareListener(reactedAddedListener, model));
    }

    private ReactionAddedModel getModel(MessageReactionAddEvent event, CachedMessage cachedMessage, ServerUser userReacting, Member member) {
        return ReactionAddedModel
                .builder()
                .reaction(event.getReaction())
                .message(cachedMessage)
                .memberReacting(member)
                .userReacting(userReacting)
                .build();
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(addedListenerList);
    }

}
