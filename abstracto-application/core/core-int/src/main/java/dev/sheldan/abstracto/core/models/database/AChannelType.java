package dev.sheldan.abstracto.core.models.database;

import net.dv8tion.jda.api.entities.ChannelType;

public enum AChannelType {
    TEXT, DM, VOICE, NEWS, CATEGORY, UNKNOWN, NEWS_THREAD, PUBLIC_THREAD, PRIVATE_THREAD, STAGE;

    public static AChannelType getAChannelType(ChannelType type) {
        switch (type) {
            case TEXT: return AChannelType.TEXT;
            case PRIVATE: return AChannelType.DM;
            case VOICE: return AChannelType.VOICE;
            case STAGE: return AChannelType.STAGE;
            case NEWS: return AChannelType.NEWS;
            case CATEGORY: return AChannelType.CATEGORY;
            case GUILD_NEWS_THREAD: return AChannelType.NEWS_THREAD;
            case GUILD_PRIVATE_THREAD: return AChannelType.PRIVATE_THREAD;
            case GUILD_PUBLIC_THREAD: return AChannelType.PUBLIC_THREAD;
            default: return AChannelType.UNKNOWN;
        }
    }

    public boolean isThread() {
        return this == PUBLIC_THREAD || this == PRIVATE_THREAD || this == NEWS_THREAD;
    }
}
