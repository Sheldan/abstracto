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
        for (MessageEmbedLink messageEmbedLink : links) {
            messageRaw = messageRaw.replace(messageEmbedLink.getWholeUrl(), "");
            Long userEmbeddingUserInServerId = userInServerManagementService.loadUser(message.getMember()).getUserInServerId();
            Consumer<CachedMessage> cachedMessageConsumer = cachedMessage ->self.loadUserAndEmbed(message, userEmbeddingUserInServerId, cachedMessage);
            messageCache.getMessageFromCache(messageEmbedLink.getServerId(), messageEmbedLink.getChannelId(), messageEmbedLink.getMessageId()).thenAccept(cachedMessageConsumer)
                    .exceptionally(throwable -> {
                        log.error("Error when embedding link for message {}", message.getId(), throwable);
                        return null;
                    });
        }
        if(StringUtils.isBlank(messageRaw) && !links.isEmpty()) {
            message.delete().queue();
        }
    }

    public void loadUserAndEmbed(Message message, Long cause, CachedMessage cachedMessage) {
        messageEmbedService.embedLink(cachedMessage, message.getTextChannel(), cause , message);
    }

    @Override
    public FeatureEnum getFeature() {
        return UtilityFeature.LINK_EMBEDS;
    }
}
