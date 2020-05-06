package dev.sheldan.abstracto.modmail.models.dto;

import dev.sheldan.abstracto.core.models.FullGuild;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServerChoice {
    private FullGuild guild;
    private String reactionEmote;
}
