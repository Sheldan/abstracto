package dev.sheldan.abstracto.core.models.template.display;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

@Getter
@Builder
@Setter

public class EmoteDisplay {
    private String markDown;
    private Long emoteId;
    private String emoteKey;
    private Boolean animated;
    private String imageUrl;

    public static EmoteDisplay fromEmote(CustomEmoji emote) {
        return EmoteDisplay
                .builder()
                .emoteId(emote.getIdLong())
                .emoteKey(emote.getName())
                .animated(emote.isAnimated())
                .markDown(emote.getAsMention())
                .build();
    }
}
