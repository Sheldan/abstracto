package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReaction;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ReactionServiceBean implements ReactionService {

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private BotService botService;

    @Override
    public CompletableFuture<Void> removeReactionFromMessage(CachedReaction reaction, CachedMessage cachedMessage) {
        return messageService.loadMessageFromCachedMessage(cachedMessage).thenCompose(message -> removeReactionFromMessage(reaction, message));
    }

    @Override
    public CompletableFuture<Void> removeReactionFromMessage(CachedReaction reaction, Message message) {
        return botService.retrieveUserById(reaction.getUser().getUserId()).thenCompose(user -> {
            if(reaction.getEmote().getCustom()) {
                return emoteService.getEmoteFromCachedEmote(reaction.getEmote()).thenCompose(emote ->
                    message.removeReaction(emote, user).submit()
                );
            } else {
                return message.removeReaction(reaction.getEmote().getEmoteName(), user).submit();
            }
        });
    }
}
