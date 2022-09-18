package dev.sheldan.abstracto.linkembed.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionAddedListener;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.listener.ReactionAddedModel;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.linkembed.config.LinkEmbedFeatureDefinition;
import dev.sheldan.abstracto.linkembed.model.database.EmbeddedMessage;
import dev.sheldan.abstracto.linkembed.service.MessageEmbedMetricService;
import dev.sheldan.abstracto.linkembed.service.management.MessageEmbedPostManagementService;
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

    @Autowired
    private MessageEmbedMetricService messageEmbedMetricService;

    @Override
    public DefaultListenerResult execute(ReactionAddedModel model) {
        Long serverId = model.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(REMOVAL_EMOTE, serverId);
        if(emoteService.isReactionEmoteAEmote(model.getReaction().getEmoji(), aEmote)) {
            Long messageId = model.getMessage().getMessageId();
            Optional<EmbeddedMessage> embeddedMessageOptional = messageEmbedPostManagementService.findEmbeddedPostByMessageId(messageId);
            if(embeddedMessageOptional.isPresent()) {
                Long channelId = model.getMessage().getChannelId();
                EmbeddedMessage embeddedMessage = embeddedMessageOptional.get();
                boolean embeddedUserRemoves = embeddedMessage.getEmbeddedUser().getUserReference().getId().equals(model.getUserReacting().getUserId());
                boolean embeddingUserRemoves = embeddedMessage.getEmbeddingUser().getUserReference().getId().equals(model.getUserReacting().getUserId());
                if(embeddedUserRemoves || embeddingUserRemoves) {
                    log.info("Removing embed in message {} in channel {} in server {} because of a user reaction.", messageId, channelId, serverId);
                    messageService.deleteMessageInChannelInServer(serverId, channelId, messageId).thenAccept(aVoid -> {
                        Optional<EmbeddedMessage> innerOptional = messageEmbedPostManagementService.findEmbeddedPostByMessageId(messageId);
                        innerOptional.ifPresent(value -> messageEmbedPostManagementService.deleteEmbeddedMessage(value));
                        messageEmbedMetricService.incrementMessageEmbedDeletedMetric(embeddedUserRemoves);
                    });
                } else {
                    log.debug("Somebody besides the original author and the user embedding added the removal reaction to the message {} in channel {} in server {}.",
                            messageId, channelId, serverId);
                    return DefaultListenerResult.IGNORED;
                }

            } else {
                log.debug("Removal emote was placed on a message which was not recognized as an embedded message.");
                return DefaultListenerResult.IGNORED;
            }
            return DefaultListenerResult.PROCESSED;
        } else {
            return DefaultListenerResult.IGNORED;
        }

    }

    @Override
    public FeatureDefinition getFeature() {
        return LinkEmbedFeatureDefinition.LINK_EMBEDS;
    }

}
