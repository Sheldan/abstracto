package dev.sheldan.abstracto.core.models.exception;

import dev.sheldan.abstracto.core.models.database.AEmote;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
public class EmoteConfiguredButNotUsableExceptionModel implements Serializable {
    private final AEmote emote;
}
