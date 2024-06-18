package dev.sheldan.abstracto.core.utils;

public class MessageUtils {

    private MessageUtils() {

    }

    public static String buildMessageUrl(Long serverId, Long channelId, Long messageId) {
        if(serverId == null || channelId == null || messageId == null) {
            return null;
        }
        return String.format("https://discord.com/channels/%s/%s/%s", serverId, channelId, messageId);
    }
}
