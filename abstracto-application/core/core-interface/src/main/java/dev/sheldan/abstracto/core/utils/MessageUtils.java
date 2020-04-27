package dev.sheldan.abstracto.core.utils;

public class MessageUtils {

    private MessageUtils() {

    }

    public static String buildMessageUrl(Long serverId, Long channelId, Long messageId) {
        return String.format("https://discordapp.com/channels/%s/%s/%s", serverId, channelId, messageId);
    }
}
