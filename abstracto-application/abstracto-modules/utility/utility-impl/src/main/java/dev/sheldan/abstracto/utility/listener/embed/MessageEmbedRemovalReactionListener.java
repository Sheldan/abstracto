package dev.sheldan.abstracto.utility.listener.embed;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncReactionAddedListener;
import dev.sheldan.abstracto.core.metrics.service.CounterMetric;
import dev.sheldan.abstracto.core.metrics.service.MetricService;
import dev.sheldan.abstracto.core.metrics.service.MetricTag;
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

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Optional;

import static dev.sheldan.abstracto.utility.listener.embed.MessageEmbedListener.MESSAGE_EMBEDDED;
import static dev.sheldan.abstracto.utility.listener.embed.MessageEmbedListener.MESSAGE_EMBED_ACTION;

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
    private MetricService metricService;

    private static final CounterMetric MESSAGE_EMBED_REMOVED_CREATOR = CounterMetric
            .builder()
            .name(MESSAGE_EMBEDDED)
            .tagList(Arrays.asList(MetricTag.getTag(MESSAGE_EMBED_ACTION, "removed.creator")))
            .build();

    private static final CounterMetric MESSAGE_EMBED_REMOVED_SOURCE = CounterMetric
            .builder()
            .name(MESSAGE_EMBEDDED)
            .tagList(Arrays.asList(MetricTag.getTag(MESSAGE_EMBED_ACTION, "removed.source")))
            .build();


    @Override
    public void executeReactionAdded(CachedMessage message, CachedReactions cachedReaction, ServerUser serverUser) {
        Long guildId = message.getServerId();
        AEmote aEmote = emoteService.getEmoteOrDefaultEmote(REMOVAL_EMOTE, guildId);
        if(emoteService.compareCachedEmoteWithAEmote(cachedReaction.getEmote(), aEmote)) {
            Optional<EmbeddedMessage> embeddedMessageOptional = messageEmbedPostManagementService.findEmbeddedPostByMessageId(message.getMessageId());
            if(embeddedMessageOptional.isPresent()) {
                EmbeddedMessage embeddedMessage = embeddedMessageOptional.get();
                boolean embeddedUserRemoves = embeddedMessage.getEmbeddedUser().getUserReference().getId().equals(serverUser.getUserId());
                boolean embeddingUserRemoves = embeddedMessage.getEmbeddingUser().getUserReference().getId().equals(serverUser.getUserId());
                if(embeddedUserRemoves || embeddingUserRemoves) {
                    log.info("Removing embed in message {} in channel {} in server {} because of a user reaction.", message.getMessageId(), message.getChannelId(), message.getServerId());
                    messageService.deleteMessageInChannelInServer(message.getServerId(), message.getChannelId(), message.getMessageId()).thenAccept(aVoid -> {
                        Optional<EmbeddedMessage> innerOptional = messageEmbedPostManagementService.findEmbeddedPostByMessageId(message.getMessageId());
                        innerOptional.ifPresent(value -> messageEmbedPostManagementService.deleteEmbeddedMessage(value));
                        if(embeddedUserRemoves) {
                            metricService.incrementCounter(MESSAGE_EMBED_REMOVED_SOURCE);
                        } else {
                            metricService.incrementCounter(MESSAGE_EMBED_REMOVED_CREATOR);
                        }
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

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_EMBED_REMOVED_CREATOR, "Message embeds which are created by the embedding user.");
        metricService.registerCounter(MESSAGE_EMBED_REMOVED_SOURCE, "Message embeds which are created by the embedded user.");
    }

}
