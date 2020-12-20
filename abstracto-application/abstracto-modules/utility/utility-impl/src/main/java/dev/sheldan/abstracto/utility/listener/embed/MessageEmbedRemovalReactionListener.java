package dev.sheldan.abstracto.utility.listener.embed;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionAddedListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.cache.CachedReactions;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.database.EmbeddedMessage;
import dev.sheldan.abstracto.utility.service.management.MessageEmbedPostManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class MessageEmbedRemovalReactionListener implements AsyncReactionAddedListener {

    public static final String REMOVAL_EMOTE = "removeEmbed";

    @Autowired
    private BotService botService;

    @Autowired
    private MessageEmbedPostManagementService messageEmbedPostManagementService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmoteService emoteService;

    @Override
    public void executeReactionAdded(CachedMessage message, CachedReactions cachedReaction, ServerUser serverUser) {
        Long guildId = message.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(REMOVAL_EMOTE, guildId);
        if(emoteService.compareCachedEmoteWithAEmote(cachedReaction.getEmote(), aEmote)) {
            Optional<EmbeddedMessage> embeddedMessageOptional = messageEmbedPostManagementService.findEmbeddedPostByMessageId(message.getMessageId());
            if(embeddedMessageOptional.isPresent()) {
                EmbeddedMessage embeddedMessage = embeddedMessageOptional.get();
                if(embeddedMessage.getEmbeddedUser().getUserReference().getId().equals(serverUser.getUserId())
                    || embeddedMessage.getEmbeddingUser().getUserReference().getId().equals(serverUser.getUserId())
                ) {
                    log.info("Removing embed in message {} in channel {} in server {} because of a user reaction.", message.getMessageId(), message.getChannelId(), message.getServerId());
                    messageService.deleteMessageInChannelInServer(message.getServerId(), message.getChannelId(), message.getMessageId()).thenAccept(aVoid ->{
                        Optional<EmbeddedMessage> innerOptional = messageEmbedPostManagementService.findEmbeddedPostByMessageId(message.getMessageId());
                        innerOptional.ifPresent(value -> messageEmbedPostManagementService.deleteEmbeddedMessage(value));
                    });
                } else {
                    log.trace("Somebody besides the original author and the user embedding added the removal reaction to the message {} in channel {} in server {}.",
                            message.getMessageId(), message.getChannelId(), message.getServerId());
                }

            } else {
                log.trace("Removal emote was placed on a message which was not recognized as an embedded message.");
            }
        }
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.LINK_EMBEDS;
    }

}
