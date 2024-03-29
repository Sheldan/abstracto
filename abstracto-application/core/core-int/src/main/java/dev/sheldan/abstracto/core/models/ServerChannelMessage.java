package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.utils.MessageUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;

@Getter
@Setter
@Builder
public class ServerChannelMessage {
    private Long serverId;
    private Long channelId;
    private Long messageId;

    public String getJumpUrl() {
        return MessageUtils.buildMessageUrl(serverId, channelId, messageId);
    }

    public static ServerChannelMessage fromMessage(Message message) {
        if(message == null) {
            return null;
        }
        return ServerChannelMessage
                .builder()
                .serverId(message.getGuild().getIdLong())
                .channelId(message.getChannel().getIdLong())
                .messageId(message.getIdLong())
                .build();
    }

    public static ServerChannelMessage fromCachedMessage(CachedMessage cachedMessage) {
        if(cachedMessage == null) {
            return null;
        }
        return ServerChannelMessage
                .builder()
                .serverId(cachedMessage.getServerId())
                .channelId(cachedMessage.getChannelId())
                .messageId(cachedMessage.getMessageId())
                .build();
    }
}
