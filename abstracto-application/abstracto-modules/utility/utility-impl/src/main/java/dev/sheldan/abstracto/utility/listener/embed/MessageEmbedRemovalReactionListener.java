package dev.sheldan.abstracto.utility.listener.embed;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.ReactedAddedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.database.EmbeddedMessage;
import dev.sheldan.abstracto.utility.service.management.MessageEmbedPostManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class MessageEmbedRemovalReactionListener implements ReactedAddedListener {

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
    public void executeReactionAdded(CachedMessage message, GuildMessageReactionAddEvent event, AUserInAServer userAdding) {
        Long guildId = message.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(REMOVAL_EMOTE, guildId);
        MessageReaction.ReactionEmote reactionEmote = event.getReactionEmote();
        if(emoteService.isReactionEmoteAEmote(reactionEmote, aEmote)) {
            Optional<EmbeddedMessage> embeddedMessageOptional = messageEmbedPostManagementService.findEmbeddedPostByMessageId(message.getMessageId());
            if(embeddedMessageOptional.isPresent()) {
                EmbeddedMessage embeddedMessage = embeddedMessageOptional.get();
                AUser userReacting = userAdding.getUserReference();
                if(embeddedMessage.getEmbeddedUser().getUserReference().getId().equals(userReacting.getId())
                    || embeddedMessage.getEmbeddingUser().getUserReference().getId().equals(userReacting.getId())
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

    @Override
    public Integer getPriority() {
        return ListenerPriority.HIGH;
    }
}
