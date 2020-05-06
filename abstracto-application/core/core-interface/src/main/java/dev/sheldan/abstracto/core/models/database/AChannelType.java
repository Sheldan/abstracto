package dev.sheldan.abstracto.core.models.database;

import net.dv8tion.jda.api.entities.ChannelType;

public enum AChannelType {
    TEXT, DM, VOICE, NEWS, CATEGORY, UNKOWN;

    public static AChannelType getAChannelType(ChannelType type) {
        switch (type) {
            case TEXT: return AChannelType.TEXT;
            case PRIVATE: return AChannelType.DM;
            case VOICE: return AChannelType.VOICE;
            case CATEGORY: return AChannelType.CATEGORY;
            default: return AChannelType.UNKOWN;
        }
    }
}
