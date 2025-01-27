package dev.sheldan.abstracto.statistic.emote.command.parameter;

import dev.sheldan.abstracto.statistic.emote.model.database.UsedEmoteType;

public enum UsedEmoteTypeParameter {
    REACTION,
    MESSAGE;

    public static UsedEmoteType convertToUsedEmoteType(UsedEmoteTypeParameter usedEmoteTypeParameter) {
        if(usedEmoteTypeParameter == null) {
            return null;
        }
        return switch (usedEmoteTypeParameter) {
            case MESSAGE -> UsedEmoteType.MESSAGE;
            case REACTION -> UsedEmoteType.REACTION;
        };
    }
}
