package dev.sheldan.abstracto.core.models.utils;

import dev.sheldan.abstracto.core.models.AChannelType;
import net.dv8tion.jda.api.entities.ChannelType;

public class ChannelUtils {
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
