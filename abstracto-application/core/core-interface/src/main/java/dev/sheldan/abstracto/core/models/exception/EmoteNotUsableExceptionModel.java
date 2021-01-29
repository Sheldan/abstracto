package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Emote;

import java.io.Serializable;

@Getter
@Builder
public class EmoteNotUsableExceptionModel implements Serializable {
    private transient Emote emote;
}
