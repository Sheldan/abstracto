package dev.sheldan.abstracto.utility.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Emote;

@Getter
@Setter
@Builder
public class ShowEmoteLog{
    private Emote emote;
}
