package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AEmote;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Emote;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class FullEmote implements Serializable {
    private AEmote fakeEmote;
    private transient Emote emote;

    public String getEmoteRepr() {
        if(!fakeEmote.getCustom()) {
            return fakeEmote.getEmoteKey();
        } else if(emote != null) {
            return emote.getAsMention();
        } else {
            return fakeEmote.getEmoteId().toString();
        }
    }
}
