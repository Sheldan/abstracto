package dev.sheldan.abstracto.utility.listener.embed;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.MessageReceivedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.models.MessageEmbedLink;
import dev.sheldan.abstracto.utility.service.MessageEmbedService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Component
@Slf4j
public class MessageEmbedListener implements MessageReceivedListener {

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MessageEmbedService messageEmbedService;

    @Autowired
    private MessageEmbedListener self;

    @Override
    public void execute(Message message) {
        String messageRaw = message.getContentRaw();
        List<MessageEmbedLink> links = messageEmbedService.getLinksInMessage(messageRaw);
        if(!links.isEmpty()) {
            log.trace("We found {} links to embed in message {} in channel {} in guild {}.", links.size(), message.getId(), message.getChannel().getId(), message.getGuild().getId());
            Long userEmbeddingUserInServerId = userInServerManagementService.loadUser(message.getMember()).getUserInServerId();
            for (MessageEmbedLink messageEmbedLink : links) {
                if(!messageEmbedLink.getServerId().equals(message.getGuild().getIdLong())) {
                    log.info("Link for message {} was from a foreign server {}. Do not embed.", messageEmbedLink.getMessageId(), messageEmbedLink.getServerId());
                    continue;
                }
                messageRaw = messageRaw.replace(messageEmbedLink.getWholeUrl(), "");
                Consumer<CachedMessage> cachedMessageConsumer = cachedMessage ->self.loadUserAndEmbed(message, userEmbeddingUserInServerId, cachedMessage);
                messageCache.getMessageFromCache(messageEmbedLink.getServerId(), messageEmbedLink.getChannelId(), messageEmbedLink.getMessageId())
                        .thenAccept(cachedMessageConsumer)
                        .exceptionally(throwable -> {
                            log.error("Error when embedding link for message {}", message.getId(), throwable);
                            return null;
                        });
            }
        }
        if(StringUtils.isBlank(messageRaw) && !links.isEmpty()) {
            message.delete().queue();
        }
    }

    @Transactional
    public void loadUserAndEmbed(Message message, Long cause, CachedMessage cachedMessage) {
        log.info("Embedding link to message {} in channel {} in server {} to channel {} and server {}.",
                cachedMessage.getMessageId(), cachedMessage.getChannelId(), cachedMessage.getServerId(), message.getChannel().getId(), message.getGuild().getId());
        messageEmbedService.embedLink(cachedMessage, message.getTextChannel(), cause , message);
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.LINK_EMBEDS;
    }
}
