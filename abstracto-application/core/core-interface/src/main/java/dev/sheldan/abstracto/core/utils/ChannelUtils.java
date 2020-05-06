package dev.sheldan.abstracto.core.utils;

public class ChannelUtils {

    private ChannelUtils() {

    }

    public static String buildChannelUrl(Long serverId, Long channelId) {
        return String.format("https://discordapp.com/channels/%s/%s/", serverId, channelId);
    }
}
