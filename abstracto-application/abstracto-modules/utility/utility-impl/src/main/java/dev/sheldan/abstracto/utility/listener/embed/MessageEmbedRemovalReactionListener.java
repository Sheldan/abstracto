package dev.sheldan.abstracto.utility.listener.embed;

import dev.sheldan.abstracto.core.listener.ReactedAddedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.dto.EmoteDto;
import dev.sheldan.abstracto.core.models.dto.UserInServerDto;
import dev.sheldan.abstracto.core.service.Bot;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.utils.EmoteUtils;
import dev.sheldan.abstracto.utility.config.UtilityFeatures;
import dev.sheldan.abstracto.utility.models.database.EmbeddedMessage;
import dev.sheldan.abstracto.utility.service.management.MessageEmbedPostManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class MessageEmbedRemovalReactionListener implements ReactedAddedListener {

    public static final String REMOVAL_EMOTE = "removeEmbed";

    @Autowired
    private EmoteService emoteManagementService;

    @Autowired
    private Bot bot;

    @Autowired
    private MessageEmbedPostManagementServiceBean messageEmbedPostManagementService;

    @Autowired
    private MessageService messageService;

    @Override
    public void executeReactionAdded(CachedMessage message, MessageReaction reaction, UserInServerDto userAdding) {
        Long guildId = message.getServerId();
        Optional<EmoteDto> aEmote = emoteManagementService.getEmoteByName(REMOVAL_EMOTE, guildId);
        if(aEmote.isPresent()) {
            EmoteDto emote = aEmote.get();
            MessageReaction.ReactionEmote reactionEmote = reaction.getReactionEmote();
            Optional<Emote> emoteInGuild = bot.getEmote(guildId, emote);
            if(EmoteUtils.isReactionEmoteAEmote(reactionEmote, emote, emoteInGuild.orElse(null))) {
                Optional<EmbeddedMessage> embeddedMessageOptional = messageEmbedPostManagementService.findEmbeddedPostByMessageId(message.getMessageId());
                if(embeddedMessageOptional.isPresent()) {
                    EmbeddedMessage embeddedMessage = embeddedMessageOptional.get();
                    if(embeddedMessage.getEmbeddedUser().getUserReference().getId().equals(userAdding.getUser().getId())
                        || embeddedMessage.getEmbeddingUser().getUserReference().getId().equals(userAdding.getUser().getId())
                    ) {
                        messageService.deleteMessageInChannelInServer(message.getServerId(), message.getChannelId(), message.getMessageId()).thenAccept(aVoid -> {
                            messageEmbedPostManagementService.deleteEmbeddedMessageTransactional(embeddedMessage);
                        });
                    }

                }
            }
        } else {
            log.warn("Emote {} is not defined for guild {}. Embed link deletion not functional.", REMOVAL_EMOTE, guildId);
        }
    }

    @Override
    public String getFeature() {
        return UtilityFeatures.LINK_EMBEDS;
    }
}
